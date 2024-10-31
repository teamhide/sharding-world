package com.teamhide.shardingworld.example

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "orders")
@Entity
class Order(
    @Column(name = "price", nullable = false)
    val price: Int,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
