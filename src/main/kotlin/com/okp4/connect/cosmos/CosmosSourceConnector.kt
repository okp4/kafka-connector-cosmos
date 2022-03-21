package com.okp4.connect.cosmos

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigDef.Importance
import org.apache.kafka.common.utils.AppInfoParser
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.source.SourceConnector

class CosmosSourceConnector : SourceConnector() {
    var config: Map<String, String> = mapOf()

    override fun start(props: Map<String, String>) {
        config = props
    }

    override fun taskClass(): Class<out Task> = CosmosSourceTask::class.java

    override fun taskConfigs(maxTasks: Int): List<Map<String, String>> = listOf(config)

    override fun stop() {
    }

    override fun config(): ConfigDef {
        return CONFIG_DEF
    }

    override fun version(): String = VERSION

    companion object {
        const val TOPIC_CONFIG = "topic"
        const val NODE_CONFIG = "node"
        const val CHAIN_ID_CONFIG = "chain-id"

        val VERSION = AppInfoParser.getVersion()

        private val CONFIG_DEF = ConfigDef().define(
            NODE_CONFIG,
            ConfigDef.Type.STRING,
            "tcp://localhost:26657",
            Importance.HIGH,
            "<host>:<port> to Tendermint RPC interface for this chain"
        ).define(
            CHAIN_ID_CONFIG, ConfigDef.Type.STRING, "okp4-testnet-1", Importance.LOW, "The network chain ID"
        ).define(
            TOPIC_CONFIG, ConfigDef.Type.LIST, Importance.HIGH, "The topic to publish data to"
        )
    }
}
