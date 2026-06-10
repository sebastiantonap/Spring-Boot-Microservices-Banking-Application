package org.training.transactions.service;

import org.springframework.stereotype.Service;

/**
 * PIIDataHandler - Masking and validation of Personally Identifiable Information (PII).
 * OCC compliance path: PII fields must never appear in plaintext in logs, responses, or audit trails.
 * Applies to: account numbers, routing numbers, SSNs, and card numbers.
 */
@Service
public class PIIDataHandler {

    /**
     * Masks a bank account number, exposing only the last 4 digits.
     * OCC compliance path — must have test coverage.
     * Example: "123456789012" → "********9012"
     *
     * @param accountNumber PII field — the full account number
     * @return masked account number safe for logging and display
     * @throws PIIException if accountNumber is null or too short to mask
     */
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            throw new PIIException("Account number must not be null");
        }
        if (accountNumber.length() < 4) {
            throw new PIIException("Account number too short to mask safely");
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "*".repeat(accountNumber.length() - 4) + lastFour;
    }

    /**
     * Masks a bank routing number, exposing only the last 4 digits.
     * OCC compliance path — routing numbers must not appear in logs.
     * Example: "021000021" → "*****0021"
     *
     * @param routingNumber PII field — the 9-digit routing number
     * @return masked routing number safe for logging
     * @throws PIIException if routingNumber is null, non-numeric, or not 9 digits
     */
    public static String maskRoutingNumber(String routingNumber) {
        if (routingNumber == null) {
            throw new PIIException("Routing number must not be null");
        }
        if (!routingNumber.matches("\\d{9}")) {
            throw new PIIException("Routing number must be exactly 9 digits");
        }
        return "*****" + routingNumber.substring(5);
    }

    /**
     * Masks a Social Security Number, exposing only the last 4 digits.
     * OCC compliance path — SSNs must never appear in plaintext.
     * Accepts formats: "123456789" or "123-45-6789"
     *
     * @param ssn PII field — the full SSN
     * @return masked SSN in format "***-**-XXXX"
     * @throws PIIException if SSN is null or in an unrecognised format
     */
    public static String maskSSN(String ssn) {
        if (ssn == null) {
            throw new PIIException("SSN must not be null");
        }
        String digits = ssn.replaceAll("-", "");
        if (!digits.matches("\\d{9}")) {
            throw new PIIException("SSN must be 9 digits (with or without hyphens)");
        }
        return "***-**-" + digits.substring(5);
    }

    /**
     * Validates that a string field does not contain raw PII before it is persisted or logged.
     * OCC compliance path — defense-in-depth check before any write operation.
     *
     * @param fieldValue the value to inspect
     * @param fieldName  the name of the field (for error messaging)
     * @throws PIIException if the field appears to contain an unmasked account number,
     *                      routing number, or SSN
     */
    public static void assertNoRawPII(String fieldValue, String fieldName) {
        if (fieldValue == null) return;

        // Detect unmasked account number pattern (10-12 consecutive digits)
        if (fieldValue.matches(".*\\b\\d{10,12}\\b.*")) {
            throw new PIIException("Field '" + fieldName + "' appears to contain an unmasked account number");
        }
        // Detect unmasked routing number (exactly 9 digits)
        if (fieldValue.matches(".*\\b\\d{9}\\b.*")) {
            throw new PIIException("Field '" + fieldName + "' appears to contain an unmasked routing number or SSN");
        }
    }

    // -- Exception class --

    public static class PIIException extends RuntimeException {
        public PIIException(String message) {
            super(message);
        }
    }
}
