package com.okp4.connect.cosmos

import cosmos.base.tendermint.v1beta1.Query
import cosmos.base.tendermint.v1beta1.ServiceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.*
import tendermint.types.BlockOuterClass

class CosmosServiceClientTest : BehaviorSpec({

    val channel = mockk<ManagedChannel>(relaxed = true)
    val stub = mockk<ServiceGrpcKt.ServiceCoroutineStub>()

    val cosmosService = CosmosServiceClient(channel, stub)

    afterTest {
        clearMocks(stub)
    }

    given("an existing height") {
        val height = 42L
        val req = slot<Query.GetBlockByHeightRequest>()
        coEvery {
            stub.getBlockByHeight(
                capture(req),
                any()
            )
        } returns Query.GetBlockByHeightResponse.getDefaultInstance()

        `when`("requesting the corresponding block") {
            val resp = cosmosService.getBlockByHeight(height)

            then("it shall fetch the block") {
                resp shouldBe Result.success(BlockOuterClass.Block.getDefaultInstance())
                req.captured.height shouldBe height
            }
        }
    }

    given("a wrong height") {
        val height = 42L
        coEvery { stub.getBlockByHeight(any(), any()) } throws StatusException(Status.INVALID_ARGUMENT)

        `when`("requesting the corresponding block") {
            val resp = cosmosService.getBlockByHeight(height)

            then("it shall fetch the block") {
                resp should {
                    resp.isFailure
                }
                (resp.exceptionOrNull() as? StatusException)?.status shouldBe Status.INVALID_ARGUMENT
            }
        }
    }

    given("a connected cosmos client") {
        `when`("closing the connection") {
            cosmosService.close()

            then("the channel shall be shutdown") {
                verify {
                    channel.shutdown()
                }
            }
        }
    }
})
