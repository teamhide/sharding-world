package com.teamhide.shardingworld.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.teamhide.shardingworld.common.config.database.DataSourceConfig
import com.teamhide.shardingworld.common.config.database.ShardType
import com.teamhide.shardingworld.common.config.database.sharding
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

private const val URL = "/order"

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
class OrderControllerTest(
    private val tableInitializer: TableInitializer,
    private val orderRepository: OrderRepository,
    private val dataSourceConfig: DataSourceConfig,
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) : BehaviorSpec({
    afterEach {
        sharding(shardKey = 1) {
            orderRepository.deleteAllInBatch()
        }
        sharding(shardKey = 2) {
            orderRepository.deleteAllInBatch()
        }
    }

    Given("saveOrder") {
        val sql = "SELECT count(*) FROM orders"

        When("userId 1번 주문을 저장하면") {
            val shard1DataSource = dataSourceConfig.getDataSourceByShardNumber(shardNumber = 1, shardType = ShardType.WRITER)
            val shard2DataSource = dataSourceConfig.getDataSourceByShardNumber(shardNumber = 2, shardType = ShardType.WRITER)

            shard1DataSource.shouldNotBeNull()
            shard2DataSource.shouldNotBeNull()

            val request = SaveOrderRequest(userId = 1L, price = 10000)

            mockMvc.post(URL) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }

            Then("2번 샤드에 저장된다") {
                shard1DataSource.connection.use { connection ->
                    connection.prepareStatement(sql).use { statement ->
                        val resultSet = statement.executeQuery()
                        resultSet.next() shouldBe true
                        val count = resultSet.getInt(1)
                        count shouldBe 0
                    }
                }
                shard2DataSource.connection.use { connection ->
                    connection.prepareStatement(sql).use { statement ->
                        val resultSet = statement.executeQuery()
                        resultSet.next() shouldBe true
                        val count = resultSet.getInt(1)
                        count shouldBe 1
                    }
                }
            }
        }

        When("userId 2번 주문을 저장하면") {
            val shard1DataSource = dataSourceConfig.getDataSourceByShardNumber(shardNumber = 1, shardType = ShardType.WRITER)
            val shard2DataSource = dataSourceConfig.getDataSourceByShardNumber(shardNumber = 2, shardType = ShardType.WRITER)

            shard1DataSource.shouldNotBeNull()
            shard2DataSource.shouldNotBeNull()

            val request = SaveOrderRequest(userId = 2L, price = 10000)

            mockMvc.post(URL) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }

            Then("1번 샤드에 저장된다") {
                shard1DataSource.connection.use { connection ->
                    connection.prepareStatement(sql).use { statement ->
                        val resultSet = statement.executeQuery()
                        resultSet.next() shouldBe true
                        val count = resultSet.getInt(1)
                        count shouldBe 1
                    }
                }
                shard2DataSource.connection.use { connection ->
                    connection.prepareStatement(sql).use { statement ->
                        val resultSet = statement.executeQuery()
                        resultSet.next() shouldBe true
                        val count = resultSet.getInt(1)
                        count shouldBe 0
                    }
                }
            }
        }
    }
})
