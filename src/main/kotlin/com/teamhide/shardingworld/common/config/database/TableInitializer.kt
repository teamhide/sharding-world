package com.teamhide.shardingworld.common.config.database

import jakarta.annotation.PostConstruct
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TableInitializer(
    private val dataSourceConfig: DataSourceConfig,

    @Value("\${spring.flyway.locations}")
    private val migrationLocation: String,
) {
    @PostConstruct
    fun init() {
        val dataSources = dataSourceConfig.getAllDataSources()
        if (dataSources.isEmpty()) throw Exception("DataSources is empty")

        dataSources.forEach { dataSource ->
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(migrationLocation)
                .load()
            flyway.migrate()
        }
    }
}
