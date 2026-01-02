package com.fooddelivery.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    
    private static final double MOCK_SUCCESS_RATE = 0.95;
    private static final int MESSAGE_PREVIEW_LENGTH = 100;
    private static final int MIN_PHONE_LENGTH_FOR_MASK = 4;
    private static final String MASKED_PLACEHOLDER = "****";
    
    public boolean sendSms(String to, String message) {
        log.info("Sending SMS to: {}", maskPhone(to));
        log.debug("Message: {}", message.substring(0, Math.min(message.length(), MESSAGE_PREVIEW_LENGTH)) + "...");
        
        try {
            boolean success = Math.random() < MOCK_SUCCESS_RATE;
            
            if (success) {
                log.info("SMS sent successfully to: {}", maskPhone(to));
                return true;
            } else {
                log.error("Failed to send SMS to: {}", maskPhone(to));
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", maskPhone(to), e.getMessage());
            return false;
        }
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < MIN_PHONE_LENGTH_FOR_MASK) {
            return MASKED_PLACEHOLDER;
        }
        return "***" + phone.substring(phone.length() - MIN_PHONE_LENGTH_FOR_MASK);
    }
}

