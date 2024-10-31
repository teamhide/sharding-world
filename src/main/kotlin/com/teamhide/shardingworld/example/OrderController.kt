package com.teamhide.shardingworld.example

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order")
class OrderController(
    private val orderService: OrderService,
) {
    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable("orderId") orderId: Long, @RequestParam("userId") userId: Long): Order {
        return orderService.getOrder(orderId = orderId, userId = userId)
    }

    @PostMapping("")
    fun saveOrder(@RequestBody request: SaveOrderRequest) {
        orderService.saveOrder(userId = request.userId, price = request.price)
    }
}
