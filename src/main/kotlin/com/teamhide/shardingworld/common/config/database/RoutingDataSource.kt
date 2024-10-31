package com.teamhide.shardingworld.common.config.database

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager

class RoutingDataSource : AbstractRoutingDataSource() {
    companion object {
        private const val SHARD_PREFIX = "shard"
        private const val WRITER = "writer"
        private const val READER = "reader"

        fun makeShardKey(shardNumber: Int, readOnly: Boolean): String {
            val isReader = if (readOnly) {
                READER
            } else {
                WRITER
            }
            return "$SHARD_PREFIX-$shardNumber-$isReader"
        }
    }

    override fun determineCurrentLookupKey(): Any {
        val shardNumber = ShardingContextHolder.get()
        return if (TransactionSynchronizationManager.isCurrentTransactionReadOnly())
            makeShardKey(shardNumber = shardNumber, readOnly = true)
        else
            makeShardKey(shardNumber = shardNumber, readOnly = false)
    }
}
