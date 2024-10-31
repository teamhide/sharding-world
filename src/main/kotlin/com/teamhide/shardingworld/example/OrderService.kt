package com.teamhide.shardingworld.example

import com.teamhide.shardingworld.common.config.database.sharding
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
) {
    @Transactional(readOnly = true)
    fun getOrder(orderId: Long, userId: Long): Order = sharding(shardKey = userId.toInt()) {
        orderRepository.findByIdOrNull(orderId) ?: throw IllegalArgumentException()
    }

    @Transactional
    fun saveOrder(userId: Long, price: Int) = sharding(shardKey = userId.toInt()) {
        val order = Order(userId = userId, price = price)
        orderRepository.save(order)
    }
}
