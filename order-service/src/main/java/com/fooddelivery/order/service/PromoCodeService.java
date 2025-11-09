package com.fooddelivery.order.service;

import com.fooddelivery.order.entity.PromoCode;
import com.fooddelivery.order.entity.PromoCodeUsage;
import com.fooddelivery.order.exception.InvalidPromoCodeException;
import com.fooddelivery.order.repository.PromoCodeRepository;
import com.fooddelivery.order.repository.PromoCodeUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeService {
    
    private static final int PERCENTAGE_DIVISOR = 100;

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;

    /**
     * Validate and get promo code
     */
    @Transactional(readOnly = true)
    public PromoCode validatePromoCode(String code, Long userId, BigDecimal orderAmount) {
        if (code == null || code.isBlank()) {
            throw new InvalidPromoCodeException("Promo code cannot be empty");
        }

        PromoCode promoCode = promoCodeRepository.findActiveByCode(code, LocalDateTime.now())
                .orElseThrow(() -> new InvalidPromoCodeException("Invalid or expired promo code: " + code));

        if (!promoCode.isValid()) {
            throw new InvalidPromoCodeException("Promo code is not valid");
        }

        if (promoCode.getMinOrderAmount() != null && 
            orderAmount.compareTo(promoCode.getMinOrderAmount()) < 0) {
            throw new InvalidPromoCodeException(
                String.format("Minimum order amount for this promo code is %.2f", 
                              promoCode.getMinOrderAmount())
            );
        }

        if (promoCode.getMaxUsesPerUser() != null) {
            int userUsageCount = promoCodeUsageRepository.countByUserIdAndPromoCodeId(userId, promoCode.getId());
            if (userUsageCount >= promoCode.getMaxUsesPerUser()) {
                throw new InvalidPromoCodeException("You have already used this promo code maximum number of times");
            }
        }

        return promoCode;
    }

    /**
     * Calculate discount based on promo code
     */
    public BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal subtotal, BigDecimal deliveryFee) {
        BigDecimal discount;

        switch (promoCode.getDiscountType()) {
            case PERCENTAGE -> {
                discount = subtotal.multiply(promoCode.getDiscountValue())
                        .divide(BigDecimal.valueOf(PERCENTAGE_DIVISOR), 2, RoundingMode.HALF_UP);

                if (promoCode.getMaxDiscountAmount() != null && 
                    discount.compareTo(promoCode.getMaxDiscountAmount()) > 0) {
                    discount = promoCode.getMaxDiscountAmount();
                }
            }
            case FIXED_AMOUNT -> {
                discount = promoCode.getDiscountValue();
                if (discount.compareTo(subtotal) > 0) {
                    discount = subtotal;
                }
            }
            case FREE_DELIVERY -> discount = deliveryFee;
            default -> discount = BigDecimal.ZERO;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Record promo code usage
     */
    @Transactional
    public void recordUsage(PromoCode promoCode, Long userId, Long orderId) {
        PromoCodeUsage usage = new PromoCodeUsage();
        usage.setUserId(userId);
        usage.setPromoCodeId(promoCode.getId());
        usage.setOrderId(orderId);

        promoCodeUsageRepository.save(usage);

        promoCode.incrementUsage();
        promoCodeRepository.save(promoCode);

        log.info("Recorded promo code usage: code={}, user={}, order={}", 
                 promoCode.getCode(), userId, orderId);
    }
}

