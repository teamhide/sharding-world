package com.teamhide.shardingworld

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShardingworldApplication

fun main(args: Array<String>) {
    runApplication<ShardingworldApplication>(*args)
}
