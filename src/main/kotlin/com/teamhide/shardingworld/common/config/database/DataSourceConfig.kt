package com.teamhide.shardingworld.common.config.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class DataSourceConfig(
    private val shardProperties: ShardProperties,
) {
    private val dataSourceMap = makeDataSourceMap()

    companion object {
        private const val DEFAULT_SHARD_NUMBER = 1
    }

    @Bean
    fun shardingDataSource(): DataSource {
        val routingDataSource = RoutingDataSource()

        val defaultShardKey = RoutingDataSource.makeShardKey(shardNumber = DEFAULT_SHARD_NUMBER, shardType = ShardType.WRITER)
        routingDataSource.setTargetDataSources(dataSourceMap)
        routingDataSource.setDefaultTargetDataSource(dataSourceMap[defaultShardKey]!!)
        return routingDataSource
    }

    @Primary
    @Bean
    fun dataSource(@Qualifier("shardingDataSource") shardingDataSource: DataSource): DataSource {
        return LazyConnectionDataSourceProxy(shardingDataSource)
    }

    private fun createDataSource(shardDb: ShardProperties.DataSourceConfig): DataSource {
        val config = HikariConfig().apply {
            jdbcUrl = shardDb.jdbcUrl
            username = shardDb.username
            password = shardDb.password
            driverClassName = shardDb.driverClassName
            poolName = shardDb.poolName
        }
        return HikariDataSource(config)
    }

    private fun makeDataSourceMap(): MutableMap<Any, Any> {
        val dataSourceMap = mutableMapOf<Any, Any>()
        shardProperties.shards.forEach { (shardName, property) ->
            val shardNumber = extractShardNumber(shardName)
            dataSourceMap[RoutingDataSource.makeShardKey(shardNumber, ShardType.WRITER)] = createDataSource(property.writer)
            dataSourceMap[RoutingDataSource.makeShardKey(shardNumber, ShardType.READER)] = createDataSource(property.reader)
        }
        return dataSourceMap
    }

    private fun extractShardNumber(shardName: String): Int {
        return shardName.split("-")[1].toInt()
    }

    fun getShardCount(): Int = shardProperties.shards.size

    fun getAllDataSources(): List<DataSource> {
        return dataSourceMap.values.filterIsInstance<DataSource>()
    }
}
