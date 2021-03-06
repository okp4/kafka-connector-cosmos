package com.okp4.connect.cosmos

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigDef.Importance
import org.apache.kafka.common.utils.AppInfoParser
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.source.SourceConnector

class CosmosSourceConnector : SourceConnector() {
    private var config: Map<String, String> = mapOf()

    override fun start(props: Map<String, String>) {
        config = props
    }

    override fun taskClass(): Class<out Task> = CosmosSourceTask::class.java

    override fun taskConfigs(maxTasks: Int): List<Map<String, String>> = listOf(config)

    override fun stop() {
        // NTD
    }

    override fun config(): ConfigDef {
        return CONFIG_DEF
    }

    override fun version(): String = VERSION

    companion object {
        const val TOPIC_CONFIG = "topic"
        const val NODE_ADDRESS_CONFIG = "node-address"
        const val NODE_PORT_CONFIG = "node-port"
        const val CHAIN_ID_CONFIG = "chain-id"
        const val MAX_POLL_LENGTH_CONFIG = "max-poll-length"
        const val TLS_ENABLE_CONFIG = "tls-enable-config"

        val VERSION: String = AppInfoParser.getVersion()

        private val CONFIG_DEF = ConfigDef().define(
            NODE_ADDRESS_CONFIG,
            ConfigDef.Type.STRING,
            "localhost",
            Importance.HIGH,
            "Address of the Cosmos gRPC endpoint for this chain"
        ).define(
            NODE_PORT_CONFIG,
            ConfigDef.Type.INT,
            26657,
            Importance.HIGH,
            "Port of the Cosmos gRPC endpoint for this chain"
        ).define(
            CHAIN_ID_CONFIG, ConfigDef.Type.STRING, "okp4-testnet-1", Importance.LOW, "The network chain ID"
        ).define(
            TOPIC_CONFIG, ConfigDef.Type.LIST, Importance.HIGH, "The topic to publish data to"
        ).define(
            MAX_POLL_LENGTH_CONFIG,
            ConfigDef.Type.LONG,
            50,
            Importance.MEDIUM,
            "The maximum number of blocks to fetch in each poll",
        ).define(
            TLS_ENABLE_CONFIG,
            ConfigDef.Type.BOOLEAN,
            false,
            Importance.MEDIUM,
            "Enable secure transport",
        )
    }
}
