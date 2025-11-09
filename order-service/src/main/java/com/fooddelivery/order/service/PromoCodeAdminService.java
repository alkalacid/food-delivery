package com.fooddelivery.order.service;

import com.fooddelivery.order.dto.PromoCodeRequestDTO;
import com.fooddelivery.order.dto.PromoCodeResponseDTO;
import com.fooddelivery.order.entity.PromoCode;
import com.fooddelivery.order.exception.PromoCodeNotFoundException;
import com.fooddelivery.order.mapper.PromoCodeMapper;
import com.fooddelivery.order.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeAdminService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    @Transactional
    public PromoCodeResponseDTO createPromoCode(PromoCodeRequestDTO request) {
        log.info("Creating promo code: {}", request.code());

        if (request.validFrom().isAfter(request.validUntil())) {
            throw new IllegalArgumentException("Valid from date must be before valid until date");
        }

        PromoCode promoCode = promoCodeMapper.toEntity(request);

        try {
            PromoCode saved = promoCodeRepository.save(promoCode);
            log.info("Promo code created: id={}, code={}", saved.getId(), saved.getCode());
            return promoCodeMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            if (promoCodeRepository.existsByCodeIgnoreCase(request.code())) {
                throw new IllegalArgumentException("Promo code already exists: " + request.code());
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PromoCodeResponseDTO getPromoCodeById(Long id) {
        PromoCode promoCode = findPromoCodeById(id);
        return promoCodeMapper.toResponse(promoCode);
    }

    @Transactional(readOnly = true)
    public Page<PromoCodeResponseDTO> getAllPromoCodes(Pageable pageable) {
        return promoCodeRepository.findAll(pageable)
                .map(promoCodeMapper::toResponse);
    }

    @Transactional
    public PromoCodeResponseDTO updatePromoCode(Long id, PromoCodeRequestDTO request) {
        log.info("Updating promo code: id={}", id);

        PromoCode promoCode = findPromoCodeById(id);

        if (request.validFrom().isAfter(request.validUntil())) {
            throw new IllegalArgumentException("Valid from date must be before valid until date");
        }

        promoCodeMapper.updateEntity(request, promoCode);

        PromoCode updated = promoCodeRepository.save(promoCode);
        log.info("Promo code updated: id={}, code={}", updated.getId(), updated.getCode());

        return promoCodeMapper.toResponse(updated);
    }

    @Transactional
    public void deletePromoCode(Long id) {
        log.info("Deleting promo code: id={}", id);

        PromoCode promoCode = findPromoCodeById(id);
        promoCode.setDeletedAt(LocalDateTime.now());
        promoCode.setActive(false);

        promoCodeRepository.save(promoCode);
        log.info("Promo code deleted (soft): id={}", id);
    }

    private PromoCode findPromoCodeById(Long id) {
        return promoCodeRepository.findById(id)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found with id: " + id));
    }
}

