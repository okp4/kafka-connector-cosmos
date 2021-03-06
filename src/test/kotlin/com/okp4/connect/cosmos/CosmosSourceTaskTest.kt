package com.okp4.connect.cosmos

import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.errors.RetriableException
import tendermint.types.BlockOuterClass
import tendermint.types.Types

class CosmosSourceTaskTest : BehaviorSpec({
    val cosmosClient = mockk<CosmosServiceClient>()
    val cosmosSourceTask = spyk(CosmosSourceTask(), recordPrivateCalls = true)
    val props = mutableMapOf(
        CosmosSourceConnector.TOPIC_CONFIG to "topic",
        CosmosSourceConnector.NODE_ADDRESS_CONFIG to "localhost",
        CosmosSourceConnector.NODE_PORT_CONFIG to "25568",
        CosmosSourceConnector.CHAIN_ID_CONFIG to "okp4-testnet-1",
        CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG to "10",
        CosmosSourceConnector.TLS_ENABLE_CONFIG to "false",
    )

    afterTest {
        clearMocks(cosmosClient, cosmosSourceTask)
    }

    mockkObject(CosmosServiceClient)
    coEvery {
        CosmosServiceClient.with(any(), any(), any())
    } returns cosmosClient

    fun mockForPoll(offset: Int?, height: Int, failAt: Int?, closedAfter: Int?) {
        cosmosSourceTask.initialize(
            mockk {
                every {
                    offsetStorageReader()
                } returns mockk {
                    every {
                        offset(any() as Map<String, String>)
                    } answers {
                        offset?.let { mapOf(CosmosSourceTask.HEIGHT_FIELD to it.toLong()) }
                    }
                }
            }
        )

        var callCount = 0
        every { cosmosClient.isClosed() } answers {
            (closedAfter != null && callCount >= closedAfter)
        }

        coEvery {
            cosmosClient.getBlockByHeight(any())
        } answers {
            ++callCount
            val reqHeight = (offset ?: 0) + callCount
            if (callCount == failAt)
                Result.failure(StatusException(Status.DEADLINE_EXCEEDED))
            else if (reqHeight > height)
                Result.failure(StatusException(Status.INVALID_ARGUMENT.withDescription("hop hop hop stop!")))
            else
                Result.success(BlockOuterClass.Block.newBuilder().setHeader(Types.Header.newBuilder().setHeight(reqHeight.toLong())).build())
        }
    }

    given("A cosmos service") {
        withData(
            mapOf(
                "poll stopped by reaching max poll" to arrayOf(10, 5, 0, 5),
                "poll stopped by reaching height" to arrayOf(10, 15, 0, 10),
                "limit case: reach both height and max poll" to arrayOf(10, 10, 0, 10),
                "poll stopped by reaching max poll (with offset)" to arrayOf(15, 5, 5, 5),
                "poll stopped by reaching height (with offset)" to arrayOf(15, 15, 5, 10),
                "limit case: reach both height and max poll (with offset)" to arrayOf(15, 10, 5, 10),
                "offset is last block" to arrayOf(5, 10, 5, 0),
            )
        ) { (height, maxPoll, offset, pollLen) ->
            and("an height of $height") {
                and("a max poll length of $maxPoll") {
                    props[CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG] = maxPoll.toString()

                    and("an offset of $offset") {
                        mockForPoll(offset.takeUnless { it < 1 }, height, null, null)
                        cosmosSourceTask.start(props)

                        When("poll is called") {
                            val resp = cosmosSourceTask.poll()

                            then("it shall poll $pollLen blocks") {
                                resp.size shouldBe pollLen
                                if (pollLen > 0) {
                                    resp.last().sourceOffset() shouldBe mapOf(CosmosSourceTask.HEIGHT_FIELD to offset + pollLen)
                                }

                                var nbCalls = pollLen
                                if (height - offset < maxPoll) {
                                    nbCalls += 1
                                }
                                coVerify(exactly = nbCalls) {
                                    cosmosClient.getBlockByHeight(any())
                                }
                            }
                        }
                    }
                }
            }
        }

        When("an error occurred during poll") {
            mockForPoll(null, 4, 3, null)
            props[CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG] = "10"
            cosmosSourceTask.start(props)

            then("it shall complete exceptionally") {
                val thrown = shouldThrow<RetriableException> {
                    cosmosSourceTask.poll()
                }

                (thrown.cause as StatusException).status shouldBe Status.DEADLINE_EXCEEDED

                coVerifyOrder {
                    cosmosClient.getBlockByHeight(1)
                    cosmosClient.getBlockByHeight(2)
                    cosmosClient.getBlockByHeight(3)
                }
            }
        }

        When("the client is closed during poll") {
            mockForPoll(null, 4, null, 3)
            props[CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG] = "10"
            cosmosSourceTask.start(props)

            then("it shall interrupt poll") {
                val resp = cosmosSourceTask.poll()
                resp.size shouldBe 3

                coVerifyOrder {
                    cosmosClient.getBlockByHeight(1)
                    cosmosClient.getBlockByHeight(2)
                    cosmosClient.getBlockByHeight(3)
                }
            }
        }

        When("stop is called") {
            props[CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG] = "10"
            cosmosSourceTask.start(props)

            every {
                cosmosClient.close()
            } answers {}
            cosmosSourceTask.stop()

            then("it shall close the cosmos client") {
                verify(exactly = 1) { cosmosClient.close() }
            }
        }

        When("poll is called multiple times") {
            mockForPoll(10, 50, null, null)
            props[CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG] = "2"
            cosmosSourceTask.start(props)

            val resp1 = cosmosSourceTask.poll()
            val resp2 = cosmosSourceTask.poll()
            then("the offset is not reset") {
                resp1.size shouldBe 2
                resp2.size shouldBe 2

                resp1.last().sourceOffset() shouldBe mapOf(CosmosSourceTask.HEIGHT_FIELD to 12)
                resp2.last().sourceOffset() shouldBe mapOf(CosmosSourceTask.HEIGHT_FIELD to 14)

                coVerifyOrder {
                    cosmosClient.getBlockByHeight(11)
                    cosmosClient.getBlockByHeight(12)
                    cosmosClient.getBlockByHeight(13)
                    cosmosClient.getBlockByHeight(14)
                }
            }
        }
    }

    given("A config") {
        withData(
            mapOf(
                "Fail without config: " + CosmosSourceConnector.TOPIC_CONFIG to CosmosSourceConnector.TOPIC_CONFIG,
                "Fail without config: " + CosmosSourceConnector.NODE_ADDRESS_CONFIG to CosmosSourceConnector.NODE_ADDRESS_CONFIG,
                "Fail without config: " + CosmosSourceConnector.NODE_PORT_CONFIG to CosmosSourceConnector.NODE_PORT_CONFIG,
                "Fail without config: " + CosmosSourceConnector.CHAIN_ID_CONFIG to CosmosSourceConnector.CHAIN_ID_CONFIG,
                "Fail without config: " + CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG to CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG,
                "Fail without config: " + CosmosSourceConnector.TLS_ENABLE_CONFIG to CosmosSourceConnector.TLS_ENABLE_CONFIG,
            )
        ) { missingProp ->
            val config = props.toMutableMap()

            config.remove(missingProp)
            When("start is called with a config without $missingProp") {
                then("it shall complete exceptionally") {
                    shouldThrow<ConnectException> {
                        cosmosSourceTask.start(config)
                    }
                }
            }
        }
    }
})
