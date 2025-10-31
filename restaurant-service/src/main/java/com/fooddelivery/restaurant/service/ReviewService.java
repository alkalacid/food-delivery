package com.fooddelivery.restaurant.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.restaurant.dto.ReviewRequestDTO;
import com.fooddelivery.restaurant.dto.ReviewResponseDTO;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.entity.Review;
import com.fooddelivery.restaurant.exception.DuplicateReviewException;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.mapper.ReviewMapper;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import com.fooddelivery.restaurant.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewMapper reviewMapper;
    
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getRestaurantReviews(Long restaurantId, int page, int size) {
        log.info("Fetching reviews for restaurant: {}, page: {}, size: {}", restaurantId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(reviewMapper::toResponse);
    }
    
    @Transactional
    @CacheEvict(value = "restaurants", key = "#restaurantId")
    public ReviewResponseDTO createReview(Long restaurantId, ReviewRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Creating review for restaurant: {} by user: {}", restaurantId, userId);
        
        Restaurant restaurant = restaurantRepository.findActiveById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found"));
        
        Review review = reviewMapper.toEntity(request);
        review.setRestaurant(restaurant);
        review.setUserId(userId);
        
        Review saved;
        try {
            saved = reviewRepository.save(review);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            if (reviewRepository.existsByUserIdAndOrderId(userId, request.orderId())) {
                throw new DuplicateReviewException("You have already reviewed this order");
            }
            throw new DuplicateReviewException("Review creation failed - duplicate data");
        }
        
        restaurant.updateRating(request.rating());
        restaurantRepository.save(restaurant);
        
        log.info("Review created with id: {}, updated restaurant rating to: {}", 
                 saved.getId(), restaurant.getAverageRating());
        return reviewMapper.toResponse(saved);
    }
}

