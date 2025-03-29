package org.example.testonetoone.service

import jakarta.persistence.EntityNotFoundException
import org.example.testonetoone.domain.Order
import org.example.testonetoone.domain.Review
import org.example.testonetoone.repository.OrderRepository
import org.example.testonetoone.repository.ReviewRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class TestOrderService(
  private val orderRepository: OrderRepository,
  private val reviewRepository: ReviewRepository
) {

  fun createOrderWithReview(): Pair<Long, Long> {
    val review = Review(
      content = "good prod",
      rating = 10
    )
    val savedReview = reviewRepository.save(review)

    // 주문 생성 및 리뷰 연결
    val order = Order(
      orderNumber = "ORD-${System.currentTimeMillis()}",
      review = savedReview
    )
    val savedOrder = orderRepository.save(order)

    return Pair(savedOrder.id!!, savedReview.id!!)
  }

  fun deleteOnlyOrder(orderId: Long) {
    orderRepository.deleteById(orderId)
  }

  fun deleteOnlyReview(reviewId: Long) {
    reviewRepository.deleteById(reviewId)
  }

  fun deleteOrderFirst(orderId: Long, reviewId: Long) {
    orderRepository.deleteById(orderId)
    reviewRepository.deleteById(reviewId)
  }

  fun deleteReviewFirst(orderId: Long, reviewId: Long) {
    reviewRepository.deleteById(reviewId)
    orderRepository.deleteById(orderId)
  }

  @Transactional(readOnly = true)
  fun getOrderById(orderId: Long): Order {
    return orderRepository.findById(orderId)
      .orElseThrow { EntityNotFoundException("Order not found with id: $orderId") }
  }

  @Transactional(readOnly = true)
  fun getReviewById(reviewId: Long): Review {
    return reviewRepository.findById(reviewId)
      .orElseThrow { EntityNotFoundException("Review not found with id: $reviewId") }
  }

}
