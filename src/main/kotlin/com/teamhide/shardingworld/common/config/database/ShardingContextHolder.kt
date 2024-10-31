package com.teamhide.shardingworld.common.config.database

class ShardingContextHolder {
    companion object {
        private val contextHolder = ThreadLocal.withInitial { 1 }

        fun get(): Int = contextHolder.get()

        fun set(shardNumber: Int) = contextHolder.set(shardNumber)

        fun clear() = contextHolder.remove()
    }
}
