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
    private var shardCount: Int = 0

    @Bean
    fun shardingDataSource(): DataSource {
        val routingDataSource = RoutingDataSource()
        val dataSourceMap = mutableMapOf<Any, Any>()

        for ((shardName, property) in shardProperties.shards.entries) {
            val shardNumber = extractShardNumber(shardName = shardName)
            val writerShardKey = RoutingDataSource.makeShardKey(shardNumber = shardNumber, shardType = ShardType.WRITER)
            val readerShardKey = RoutingDataSource.makeShardKey(shardNumber = shardNumber, shardType = ShardType.READER)

            val writerDataSource = createDataSource(shardDb = property.writer)
            val readerDataSource = createDataSource(shardDb = property.reader)

            dataSourceMap[writerShardKey] = writerDataSource
            dataSourceMap[readerShardKey] = readerDataSource

            increaseShardCount()
        }

        val defaultShardKey = RoutingDataSource.makeShardKey(shardNumber = 1, shardType = ShardType.WRITER)
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

    private fun extractShardNumber(shardName: String): Int {
        return shardName.split("-")[1].toInt()
    }

    private fun increaseShardCount() {
        this.shardCount += 1
    }

    fun getShardCount(): Int = this.shardCount
}
