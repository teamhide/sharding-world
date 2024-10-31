package com.teamhide.shardingworld.common.config.database

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class ApplicationContextProvider : ApplicationContextAware {
    companion object {
        private lateinit var context: ApplicationContext

        fun <T> getBean(clazz: Class<T>): T {
            return this.context.getBean(clazz)
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}

fun <T> sharding(shardKey: Int, func: () -> T): T {
    val dataSourceConfig = ApplicationContextProvider.getBean(DataSourceConfig::class.java)
    val shardCount = dataSourceConfig.getShardCount()

    ShardingContextHolder.set(shardKey.mod(shardCount) + 1)
    val result = func.invoke()
    ShardingContextHolder.clear()

    return result
}
