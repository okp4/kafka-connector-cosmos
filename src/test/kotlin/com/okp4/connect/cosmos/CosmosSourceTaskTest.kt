package com.okp4.connect.cosmos

import cosmos.base.tendermint.v1beta1.Query
import cosmos.base.tendermint.v1beta1.ServiceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class CosmosSourceTaskTest : BehaviorSpec({
    val channel = mockk<ManagedChannel>(relaxed = true)
    val req = slot<Query.GetBlockByHeightRequest>()
    val stub = mockk<ServiceGrpcKt.ServiceCoroutineStub>()

    val cosmosSourceTask = spyk(CosmosSourceTask(), recordPrivateCalls = true)
    val props = mutableMapOf(
        "topic" to "topic",
        "node-address" to "localhost",
        "node-port" to "25568",
        "chain-id" to "okp4-testnet-1",
        "max-poll-length" to "10",
        "tls-enable-config" to "false",
    )

    afterTest {
        clearMocks(stub)
    }

    mockkObject(CosmosServiceClient)
    coEvery {
        CosmosServiceClient.with(any(), any(), any())
    } returns CosmosServiceClient(channel, stub)

    given("an existing height") {
        every {
            cosmosSourceTask getProperty "lastBlockHeightFromOffsetStorage"
        } returns 42L

        `when`("requesting to poll blocks with max poll length at 10") {
            props["max-poll-length"] = "10"
            cosmosSourceTask.start(props)

            then("it shall poll 10 blocks") {
                coEvery {
                    stub.getBlockByHeight(
                        capture(req),
                        any()
                    )
                } returns Query.GetBlockByHeightResponse.getDefaultInstance()
                val resp = cosmosSourceTask.poll()
                resp.size shouldBe 10
            }

            then("it shall fail to poll 10 blocks") {
                coEvery { stub.getBlockByHeight(any(), any()) } throws StatusException(Status.INVALID_ARGUMENT)

                val resp = cosmosSourceTask.poll()
                resp.size shouldBe 0
            }

            then("it shall throw an exception") {
                coEvery { stub.getBlockByHeight(any(), any()) } throws StatusException(Status.DEADLINE_EXCEEDED)

                shouldThrow<StatusException> {
                    cosmosSourceTask.poll()
                }
            }
        }

        `when`("requesting to poll blocks with max poll length at 1") {
            props["max-poll-length"] = "1"
            cosmosSourceTask.start(props)

            then("it shall poll 1 block") {
                coEvery {
                    stub.getBlockByHeight(
                        capture(req),
                        any()
                    )
                } returns Query.GetBlockByHeightResponse.getDefaultInstance()
                val resp = cosmosSourceTask.poll()
                resp.size shouldBe 1
            }
        }

        `when`("stopping the task") {
            cosmosSourceTask.start(props)
            cosmosSourceTask.stop()

            then("the channel shall be shutdown") {
                verify {
                    channel.shutdown()
                }
            }
        }
    }
})
