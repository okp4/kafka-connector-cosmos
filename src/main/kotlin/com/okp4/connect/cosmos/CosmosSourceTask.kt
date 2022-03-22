package com.okp4.connect.cosmos

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask

class CosmosSourceTask : SourceTask() {
    private var chainId: String? = null
    private var node: String? = null
    private var topic: String? = null

    private var sourcePartition: Map<String, String?> = mapOf()
    private var height: Long = 0

    override fun version(): String = CosmosSourceConnector.VERSION

    override fun start(props: Map<String, String>) {
        chainId = props[CosmosSourceConnector.CHAIN_ID_CONFIG]
        node = props[CosmosSourceConnector.NODE_CONFIG]
        topic = props[CosmosSourceConnector.TOPIC_CONFIG]

        sourcePartition = mapOf(
            CHAIN_ID_FIELD to chainId,
            NODE_FIELD to node
        )
    }

    // TODO: ⚠️ fake code - implement-me correctly here!
    @Throws(InterruptedException::class)
    override fun poll(): List<SourceRecord>? {
        val sourceRecords = when {
            (0..10).shuffled().last() == 5 -> {
                height++

                listOf(
                    SourceRecord(
                        sourcePartition,
                        mapOf(HEIGHT_FIELD to height),
                        topic,
                        null,
                        null,
                        null,
                        Schema.STRING_SCHEMA,
                        "This is payload for block $height",
                        System.currentTimeMillis()
                    )
                )
            }
            else -> {
                Thread.sleep(1000)
                null
            }
        }
        return sourceRecords
    }

    override fun stop() {
        // TODO implement-me!
    }

    companion object {
        const val CHAIN_ID_FIELD = "chain-id"
        const val NODE_FIELD = "node"
        const val HEIGHT_FIELD = "height"
    }
}
