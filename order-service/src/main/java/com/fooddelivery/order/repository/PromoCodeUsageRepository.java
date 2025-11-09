package com.fooddelivery.order.repository;

import com.fooddelivery.order.entity.PromoCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {

    /**
     * Count how many times a user has used a specific promo code
     */
    int countByUserIdAndPromoCodeId(Long userId, Long promoCodeId);
}

