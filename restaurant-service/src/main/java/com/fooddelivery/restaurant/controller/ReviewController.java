package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.ReviewRequestDTO;
import com.fooddelivery.restaurant.dto.ReviewResponseDTO;
import com.fooddelivery.restaurant.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @GetMapping
    public Page<ReviewResponseDTO> getRestaurantReviews(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return reviewService.getRestaurantReviews(restaurantId, page, size);
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponseDTO createReview(
            @PathVariable Long restaurantId,
            @Valid @RequestBody ReviewRequestDTO request) {
        return reviewService.createReview(restaurantId, request);
    }
}

