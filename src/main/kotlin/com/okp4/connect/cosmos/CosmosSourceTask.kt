package com.okp4.connect.cosmos

import kotlinx.coroutines.runBlocking
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask
import java.util.Collections

class CosmosSourceTask : SourceTask() {
    private lateinit var topic: String
    private var maxPollLength: Long = 0

    private var sourcePartition: Map<String, String?> = mapOf()

    private lateinit var serviceClient: CosmosServiceClient

    override fun version(): String = CosmosSourceConnector.VERSION

    override fun start(props: Map<String, String>) {
        val nodeAddress = props[CosmosSourceConnector.NODE_ADDRESS_CONFIG] ?: throw Exception("Node address cannot be empty")
        val nodePort = props[CosmosSourceConnector.NODE_PORT_CONFIG]?.toInt() ?: throw Exception("Node port cannot be empty")
        val tlsEnable = props[CosmosSourceConnector.TLS_ENABLE_CONFIG].toBoolean()
        val chainId = props[CosmosSourceConnector.CHAIN_ID_CONFIG]
        topic = props[CosmosSourceConnector.TOPIC_CONFIG] ?: throw Exception("Topic cannot be empty")
        maxPollLength = props[CosmosSourceConnector.MAX_POLL_LENGTH_CONFIG]?.toLong() ?: 1000

        sourcePartition = mapOf(
            CHAIN_ID_FIELD to chainId,
            NODE_FIELD to nodeAddress
        )
        serviceClient = CosmosServiceClient(nodeAddress, nodePort, tlsEnable)
    }

    @Throws(InterruptedException::class)
    override fun poll(): List<SourceRecord> {
        // Get last block height from offset storage
        var height = context
            .offsetStorageReader()
            .offset(Collections.singletonMap<String, Any>("BLOCK_FIELD", "okp4"/* TODO: find which value goes here*/))[HEIGHT_FIELD] as Long

        val sourceRecords: MutableList<SourceRecord> = mutableListOf()

        var i: Long = 0

        runBlocking {
            while (i <= maxPollLength) {
                val result = serviceClient.getBlockByHeight(height)
                if (result.isFailure) {
                    break
                } else {
                    sourceRecords.add(
                        SourceRecord(
                            sourcePartition,
                            mapOf(HEIGHT_FIELD to height),
                            topic,
                            null,
                            null,
                            null,
                            Schema.BYTES_SCHEMA,
                            result.getOrNull()?.toByteArray(),
                            System.currentTimeMillis()
                        )
                    )
                }
                ++height
                ++i
            }
        }
        return sourceRecords
    }

    override fun stop() {
        serviceClient.close()
    }

    companion object {
        const val CHAIN_ID_FIELD = "chain-id"
        const val NODE_FIELD = "node"
        const val HEIGHT_FIELD = "height"
    }
}
