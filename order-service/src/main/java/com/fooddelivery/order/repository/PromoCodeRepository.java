package com.fooddelivery.order.repository;

import com.fooddelivery.order.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    /**
     * Find active and valid promo code
     */
    @Query("SELECT p FROM PromoCode p WHERE UPPER(p.code) = UPPER(:code) " +
           "AND p.active = true " +
           "AND p.deletedAt IS NULL " +
           "AND p.validFrom <= :now " +
           "AND p.validUntil >= :now")
    Optional<PromoCode> findActiveByCode(@Param("code") String code, @Param("now") LocalDateTime now);

    /**
     * Check if promo code exists
     */
    boolean existsByCodeIgnoreCase(String code);
}

