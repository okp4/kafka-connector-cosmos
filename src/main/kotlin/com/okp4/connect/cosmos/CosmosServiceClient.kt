package com.okp4.connect.cosmos

import cosmos.base.tendermint.v1beta1.Query
import cosmos.base.tendermint.v1beta1.ServiceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import tendermint.types.BlockOuterClass
import java.io.Closeable
import java.util.concurrent.TimeUnit

class CosmosServiceClient(private val channel: ManagedChannel, private val stub: ServiceGrpcKt.ServiceCoroutineStub) : Closeable {
    suspend fun getBlockByHeight(height: Long): Result<BlockOuterClass.Block> =
        stub.runCatching {
            getBlockByHeight(
                Query.GetBlockByHeightRequest.newBuilder()
                    .setHeight(height)
                    .build()
            ).block
        }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    fun isClosed(): Boolean = channel.isTerminated

    companion object Factory {
        fun with(address: String, port: Int, tls: Boolean): CosmosServiceClient {
            val channel = ManagedChannelBuilder
                .forAddress(address, port)
                .apply {
                    if (tls) useTransportSecurity()
                    else usePlaintext()
                }.build()

            return CosmosServiceClient(channel, ServiceGrpcKt.ServiceCoroutineStub(channel))
        }
    }
}
