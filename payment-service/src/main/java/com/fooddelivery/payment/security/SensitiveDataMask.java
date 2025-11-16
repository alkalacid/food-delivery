package com.fooddelivery.payment.security;

/**
 * Utility for masking sensitive data in logs and responses
 * PCI DSS Compliance: Never log full card numbers, CVV, or sensitive data
 */
public class SensitiveDataMask {

    private static final int CARD_NUMBER_MIN_LENGTH = 13;
    private static final int CARD_NUMBER_MAX_LENGTH = 19;
    private static final int CVV_MIN_LENGTH = 3;
    private static final int CVV_MAX_LENGTH = 4;
    
    /**
     * Validate that string doesn't contain sensitive data patterns
     * Returns true if safe to log
     */
    public static boolean isSafeToLog(String data) {
        if (data == null) {
            return true;
        }
        
        String cardPattern = ".*\\b\\d{" + CARD_NUMBER_MIN_LENGTH + "," + CARD_NUMBER_MAX_LENGTH + "}\\b.*";
        if (data.matches(cardPattern)) {
            return false;
        }
        
        String cvvPattern = ".*\\bcvv\\s*[:=]?\\s*\\d{" + CVV_MIN_LENGTH + "," + CVV_MAX_LENGTH + "}\\b.*";
        if (data.matches(cvvPattern)) {
            return false;
        }
        
        // Check for common sensitive keywords
        String lowerData = data.toLowerCase();
        return !lowerData.contains("password") &&
               !lowerData.contains("secret") &&
               !lowerData.contains("token") &&
               !lowerData.contains("api_key");
    }
}

