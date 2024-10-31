package com.teamhide.shardingworld.common.config.database

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager

class RoutingDataSource : AbstractRoutingDataSource() {
    companion object {
        private const val SHARD_PREFIX = "shard"

        fun makeShardKey(shardNumber: Int, shardType: ShardType): String {
            return "$SHARD_PREFIX-$shardNumber-${shardType.name.lowercase()}"
        }
    }

    override fun determineCurrentLookupKey(): Any {
        val shardNumber = ShardingContextHolder.get()
        val isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
        val shardType = if (isReadOnly) ShardType.READER else ShardType.WRITER
        return makeShardKey(shardNumber = shardNumber, shardType = shardType)
    }
}
