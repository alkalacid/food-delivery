package com.fooddelivery.order.service;

import com.fooddelivery.order.config.ExternalServicesProperties;
import com.fooddelivery.order.config.PricingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {
    
    private static final double DEFAULT_DISTANCE_KM = 5.0;
    private static final int DEFAULT_DELIVERY_TIME_MINUTES = 30;

    private final ExternalServicesProperties servicesProperties;
    private final PricingProperties pricingProperties;
    
    public BigDecimal calculateDeliveryFee(Long restaurantId, Long deliveryAddressId) {
        try {
            double distanceKm = fetchDistance(restaurantId, deliveryAddressId);
            
            BigDecimal distanceFee = pricingProperties.getDeliveryFeePerKm()
                    .multiply(BigDecimal.valueOf(distanceKm));
            BigDecimal totalFee = pricingProperties.getBaseDeliveryFee().add(distanceFee);
            
            return totalFee.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("Failed to calculate delivery fee, using default: {}", e.getMessage());
            return pricingProperties.getBaseDeliveryFee();
        }
    }
    
    public Integer calculateEstimatedDeliveryTime(Long restaurantId, Long deliveryAddressId) {
        try {
            double distanceKm = fetchDistance(restaurantId, deliveryAddressId);
            int deliveryTime = (int) Math.ceil(distanceKm * pricingProperties.getDeliveryTimePerKm());
            
            return pricingProperties.getBasePreparationTime() + deliveryTime;
        } catch (Exception e) {
            log.warn("Failed to calculate estimated time, using default");
            return pricingProperties.getBasePreparationTime() + DEFAULT_DELIVERY_TIME_MINUTES;
        }
    }
    
    private double fetchDistance(Long restaurantId, Long deliveryAddressId) {
        try {
            String restaurantUrl = servicesProperties.getRestaurant().getBaseUrl() + 
                                   servicesProperties.getRestaurant().getRestaurantsEndpoint() + 
                                   "/" + restaurantId;
            
            String addressUrl = servicesProperties.getUser().getBaseUrl() + 
                               servicesProperties.getUser().getAddressesEndpoint() + 
                               "/" + deliveryAddressId;
            
            log.debug("Would fetch restaurant from: {}", restaurantUrl);
            log.debug("Would fetch address from: {}", addressUrl);
            log.debug("Fetching distance between restaurant {} and address {}", restaurantId, deliveryAddressId);
            
            return DEFAULT_DISTANCE_KM;
        } catch (Exception e) {
            log.warn("Failed to fetch distance: {}", e.getMessage());
            return DEFAULT_DISTANCE_KM;
        }
    }
}

