package org.example.testonetoone.repository

import org.example.testonetoone.domain.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long>
