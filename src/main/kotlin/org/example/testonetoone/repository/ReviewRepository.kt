package org.example.testonetoone.repository

import org.example.testonetoone.domain.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long>
