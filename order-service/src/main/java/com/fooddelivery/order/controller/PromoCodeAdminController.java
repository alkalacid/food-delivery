package com.fooddelivery.order.controller;

import com.fooddelivery.order.dto.PromoCodeRequestDTO;
import com.fooddelivery.order.dto.PromoCodeResponseDTO;
import com.fooddelivery.order.service.PromoCodeAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints for managing promo codes
 * Only accessible by ADMIN users
 */
@RestController
@RequestMapping("/api/admin/promo-codes")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class PromoCodeAdminController {

    private final PromoCodeAdminService promoCodeAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromoCodeResponseDTO createPromoCode(@Valid @RequestBody PromoCodeRequestDTO request) {
        log.info("Admin creating promo code: {}", request.code());
        return promoCodeAdminService.createPromoCode(request);
    }

    @GetMapping("/{id}")
    public PromoCodeResponseDTO getPromoCode(@PathVariable Long id) {
        return promoCodeAdminService.getPromoCodeById(id);
    }

    @GetMapping
    public Page<PromoCodeResponseDTO> getAllPromoCodes(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return promoCodeAdminService.getAllPromoCodes(pageable);
    }

    @PutMapping("/{id}")
    public PromoCodeResponseDTO updatePromoCode(
            @PathVariable Long id,
            @Valid @RequestBody PromoCodeRequestDTO request) {
        log.info("Admin updating promo code: id={}", id);
        return promoCodeAdminService.updatePromoCode(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromoCode(@PathVariable Long id) {
        log.info("Admin deleting promo code: id={}", id);
        promoCodeAdminService.deletePromoCode(id);
    }
}

