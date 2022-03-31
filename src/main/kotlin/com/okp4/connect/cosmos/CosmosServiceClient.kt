package com.okp4.connect.cosmos

import cosmos.base.tendermint.v1beta1.Query
import cosmos.base.tendermint.v1beta1.ServiceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import tendermint.types.BlockOuterClass
import java.io.Closeable
import java.util.concurrent.TimeUnit

class CosmosServiceClient(address: String, port: Int, tls: Boolean) : Closeable {
    private val channel: ManagedChannel
    private val stub: ServiceGrpcKt.ServiceCoroutineStub

    init {
        channel = if (tls) {
            ManagedChannelBuilder.forAddress(address, port).useTransportSecurity()
        } else {
            ManagedChannelBuilder.forAddress(address, port).usePlaintext()
        }.build()

        stub = ServiceGrpcKt.ServiceCoroutineStub(channel)
    }

    suspend fun getBlockByHeight(height: Long): Result<BlockOuterClass.Block> {
        return stub.runCatching {
            stub.getBlockByHeight(
                Query.GetBlockByHeightRequest.newBuilder()
                    .setHeight(height)
                    .build()
            ).block
        }
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}
