package com.okp4.connect.cosmos

import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask
import tendermint.types.BlockOuterClass.Block

class CosmosSourceTask : SourceTask() {
    private lateinit var topic: String
    private var maxPollLength: Long = 0

    private var sourcePartition: Map<String, String?> = mapOf()
    private fun Map<String, String>.mustGet(prop: String): String =
        this[prop] ?: throw ConnectException("Invalid configuration. Property $prop cannot be empty")

    private lateinit var serviceClient: CosmosServiceClient

    private val lastBlockHeightFromOffsetStorage: Long
        get() =
            context
                .offsetStorageReader()
                .offset(sourcePartition)
                .getOrDefault(HEIGHT_FIELD, -1L) as Long

    override fun version(): String = CosmosSourceConnector.VERSION

    override fun start(props: Map<String, String>) {
        val nodeAddress = props.mustGet(CosmosSourceConnector.NODE_ADDRESS_CONFIG)
        val nodePort = props.mustGet(CosmosSourceConnector.NODE_PORT_CONFIG).toInt()
        val tlsEnable = props.mustGet(CosmosSourceConnector.TLS_ENABLE_CONFIG).toBoolean()
        val chainId = props.mustGet(CosmosSourceConnector.CHAIN_ID_CONFIG)
        topic = props.mustGet(CosmosSourceConnector.TOPIC_CONFIG)
        maxPollLength = props.mustGet(CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG).toLong()

        sourcePartition = mapOf(
            CHAIN_ID_FIELD to chainId,
            NODE_FIELD to nodeAddress
        )
        serviceClient = CosmosServiceClient.with(nodeAddress, nodePort, tlsEnable)
    }

    @Throws(InterruptedException::class)
    override fun poll(): List<SourceRecord> {
        var height = lastBlockHeightFromOffsetStorage

        val sourceRecords: MutableList<SourceRecord> = mutableListOf()

        runBlocking {
            while (sourceRecords.size <= maxPollLength && !serviceClient.isClosed()) {
                ++height
                serviceClient.getBlockByHeight(height).fold(
                    onSuccess = {
                        sourceRecords.add(asSourceRecord(it))
                    },
                    onFailure = {
                        // If the status of the exception is INVALID_ARGUMENT,
                        // it means that we reached the end of the chain
                        if ((it is StatusException) && (it.status.code == Status.Code.INVALID_ARGUMENT)) return@runBlocking
                        else throw it
                    }
                )
            }
        }
        return sourceRecords
    }

    override fun stop() {
        serviceClient.close()
    }

    private fun asSourceRecord(block: Block) =
        SourceRecord(
            sourcePartition,
            mapOf(HEIGHT_FIELD to block.header.height),
            topic,
            null,
            null,
            null,
            Schema.BYTES_SCHEMA,
            block.toByteArray(),
            System.currentTimeMillis()
        )

    companion object {
        const val CHAIN_ID_FIELD = "chain-id"
        const val NODE_FIELD = "node"
        const val HEIGHT_FIELD = "height"
    }
}
