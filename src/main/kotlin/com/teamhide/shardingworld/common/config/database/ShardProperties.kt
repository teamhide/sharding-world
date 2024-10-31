package com.teamhide.shardingworld.common.config.database

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
data class ShardProperties(
    var shards: Map<String, ShardConfig> = emptyMap()
) {
    data class ShardConfig(
        val writer: DataSourceConfig,
        val reader: DataSourceConfig
    )

    data class DataSourceConfig(
        val minimumIdle: Int,
        val jdbcUrl: String,
        val username: String,
        val password: String,
        val poolName: String,
        val driverClassName: String,
        val readOnly: Boolean
    )
}
