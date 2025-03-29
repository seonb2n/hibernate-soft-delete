package org.example.testonetoone.service

import jakarta.persistence.EntityManager
import org.example.testonetoone.repository.OrderRepository
import org.example.testonetoone.repository.ReviewRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest
@Transactional
class TestOrderServiceTest {

  private val logger = LoggerFactory.getLogger(this::class.java)

  @Autowired
  private lateinit var testOrderService: TestOrderService

  @Autowired
  private lateinit var orderRepository: OrderRepository

  @Autowired
  private lateinit var reviewRepository: ReviewRepository

  @Autowired
  private lateinit var entityManager: EntityManager

  private var orderId: Long = 0
  private var reviewId: Long = 0

  @BeforeEach
  fun setUp() {
    // 각 테스트 전에 데이터 초기화
    val (createdOrderId, createdReviewId) = testOrderService.createOrderWithReview()
    orderId = createdOrderId
    reviewId = createdReviewId

    // 영속성 컨텍스트 초기화
    entityManager.flush()
    entityManager.clear()

    logger.info("===== 테스트 시작 =====")
    logger.info("생성된 Order ID: $orderId, Review ID: $reviewId")
  }

  @AfterEach
  fun tearDown() {
    // 테스트 완료 후 DB 상태 로깅
    logger.info("===== 테스트 종료 =====")
    logger.info("========================================")
  }

  @Test
  fun testDeleteOnlyOrder() {
    logger.info("==== DeleteOnlyOrder 테스트 ====")
    // 삭제 전 상태 확인
    val orderBefore = testOrderService.getOrderById(orderId)
    logger.info("삭제 전 Order: ${orderBefore.id}, reviewId: ${orderBefore.review?.id}")

    // Order만 삭제
    logger.info("Order 삭제 실행")
    testOrderService.deleteOnlyOrder(orderId)

    // 영속성 컨텍스트 초기화
    entityManager.flush()
    entityManager.clear()

    // 삭제 후 Review 상태 확인 (Order는 이미 삭제됨)
    try {
      val reviewAfter = testOrderService.getReviewById(reviewId)
      logger.info("삭제 후 Review: ${reviewAfter.id}")
    } catch (e: Exception) {
      logger.info("Review 조회 실패: ${e.message}")
    }

    // SQL 확인용 네이티브 쿼리 실행
    val nativeReview = entityManager.createNativeQuery(
      "SELECT * FROM reviews WHERE id = $reviewId"
    ).resultList
    logger.info("네이티브 쿼리로 Review 확인: $nativeReview")
  }

  @Test
  fun testDeleteOnlyReview() {
    logger.info("==== DeleteOnlyReview 테스트 ====")
    // 삭제 전 상태 확인
    val orderBefore = testOrderService.getOrderById(orderId)
    logger.info("삭제 전 Order: ${orderBefore.id}, reviewId: ${orderBefore.review?.id}")

    // Review만 삭제
    logger.info("Review 삭제 실행")
    testOrderService.deleteOnlyReview(reviewId)

    // 영속성 컨텍스트 초기화
    entityManager.flush()
    entityManager.clear()

    // 삭제 후 Order 상태 확인
    try {
      val orderAfter = testOrderService.getOrderById(orderId)
      logger.info("삭제 후 Order: ${orderAfter.id}, reviewId: ${orderAfter.review?.id}")
    } catch (e: Exception) {
      logger.info("Order 조회 실패: ${e.message}")
    }

    // SQL 확인용 네이티브 쿼리 실행
    val nativeOrder = entityManager.createNativeQuery(
      "SELECT * FROM orders WHERE id = $orderId"
    ).resultList
    logger.info("네이티브 쿼리로 Order 확인: $nativeOrder")
  }

  @Test
  fun testDeleteOrderFirst() {
    logger.info("==== DeleteOrderFirst 테스트 ====")
    // 삭제 전 상태 확인
    val orderBefore = testOrderService.getOrderById(orderId)
    val reviewBefore = testOrderService.getReviewById(reviewId)
    logger.info("삭제 전 Order: ${orderBefore.id}, reviewId: ${orderBefore.review?.id}")
    logger.info("삭제 전 Review: ${reviewBefore.id}")

    // Order를 먼저 삭제하고 Review 삭제
    logger.info("Order 먼저 삭제 후 Review 삭제 실행")
    testOrderService.deleteOrderFirst(orderId, reviewId)

    // 영속성 컨텍스트 초기화
    entityManager.flush()
    entityManager.clear()

    // 네이티브 쿼리로 DB 상태 확인
    val nativeOrder = entityManager.createNativeQuery(
      "SELECT * FROM orders WHERE id = $orderId"
    ).resultList
    logger.info("네이티브 쿼리로 Order 확인: $nativeOrder.review")

    val nativeReview = entityManager.createNativeQuery(
      "SELECT * FROM reviews WHERE id = $reviewId"
    ).resultList
    logger.info("네이티브 쿼리로 Review 확인: $nativeReview")
  }

  @Test
  fun testDeleteReviewFirst() {
    logger.info("==== DeleteReviewFirst 테스트 ====")
    // 삭제 전 상태 확인
    val orderBefore = testOrderService.getOrderById(orderId)
    val reviewBefore = testOrderService.getReviewById(reviewId)
    logger.info("삭제 전 Order: ${orderBefore.id}, reviewId: ${orderBefore.review?.id}")
    logger.info("삭제 전 Review: ${reviewBefore.id}")

    // Review를 먼저 삭제하고 Order 삭제
    logger.info("Review 먼저 삭제 후 Order 삭제 실행")
    testOrderService.deleteReviewFirst(orderId, reviewId)

    // 영속성 컨텍스트 초기화
    entityManager.flush()
    entityManager.clear()

    // 네이티브 쿼리로 Order의 review_id 확인
    val reviewIdResult = entityManager.createNativeQuery(
      "SELECT review_id FROM orders WHERE id = :orderId"
    ).setParameter("orderId", orderId).resultList

    val reviewId = if (reviewIdResult.isNotEmpty()) reviewIdResult[0] else "null"
    logger.info("Order(id=$orderId)의 review_id: $reviewId")

    // 삭제된 Review 데이터 확인
    val deletedReviewResult = entityManager.createNativeQuery(
      "SELECT id, content, rating, deleted_at FROM reviews WHERE id = :reviewId"
    ).setParameter("reviewId", reviewId).resultList

    logger.info("Review(id=$reviewId) 삭제 상태: ${deletedReviewResult.isNotEmpty()}")
    if (deletedReviewResult.isNotEmpty()) {
      val review = deletedReviewResult[0]
      logger.info("삭제된 Review 정보: $review")
    }
  }

  @Test
  fun testNativeQueriesAfterSoftDelete() {
    logger.info("==== 네이티브 쿼리 테스트 ====")

    // 먼저 Order와 Review 모두 삭제
    testOrderService.deleteOrderFirst(orderId, reviewId)

    // 영속성 컨텍스트 초기화
    entityManager.flush()
    entityManager.clear()

    // 모든 Order와 Review 조회 (soft delete된 것 포함)
    val allOrders = entityManager.createNativeQuery(
      "SELECT * FROM orders"
    ).resultList
    logger.info("모든 Orders (soft delete 포함): $allOrders")

    val allReviews = entityManager.createNativeQuery(
      "SELECT * FROM reviews"
    ).resultList
    logger.info("모든 Reviews (soft delete 포함): $allReviews")

    // Soft Delete 되지 않은 것만 조회
    val activeOrders = entityManager.createNativeQuery(
      "SELECT * FROM orders WHERE deleted_at IS NULL"
    ).resultList
    logger.info("활성화된 Orders: $activeOrders")

    val activeReviews = entityManager.createNativeQuery(
      "SELECT * FROM reviews WHERE deleted_at IS NULL"
    ).resultList
    logger.info("활성화된 Reviews: $activeReviews")

    // Order의 review_id가 어떻게 되어있는지 확인
    val orderReviewIds = entityManager.createNativeQuery(
      "SELECT id, review_id, deleted_at FROM orders"
    ).resultList
    logger.info("Orders의 review_id 상태: $orderReviewIds")
  }
}
