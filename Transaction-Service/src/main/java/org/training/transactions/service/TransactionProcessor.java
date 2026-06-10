package org.training.transactions.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * TransactionProcessor - Core transaction processing logic for BofA retail banking.
 * Handles debits, credits, and transfers with PII field validation.
 * OCC compliance path: All transaction mutations must be validated, logged, and auditable.
 */
@Service
public class TransactionProcessor {

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("50000.00");
    private static final BigDecimal MFA_THRESHOLD = new BigDecimal("10000.00");
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");

    /**
     * Processes a debit transaction against a customer account.
     * OCC compliance path — must have test coverage.
     *
     * @param accountNumber  PII field — customer account number (must be masked in logs)
     * @param routingNumber  PII field — bank routing number (must be masked in logs)
     * @param amount         transaction amount in USD
     * @param requestingUser the authenticated user initiating the transaction
     * @return TransactionResult with status and reference ID
     * @throws TransactionException if validation fails
     */
    public TransactionResult processDebit(String accountNumber, String routingNumber,
                                          BigDecimal amount, String requestingUser) {
        // PII field — mask before logging
        validateAccountNumber(accountNumber);
        // PII field — mask before logging
        validateRoutingNumber(routingNumber);
        validateAmount(amount);
        validateDailyLimit(accountNumber, amount);

        if (amount.compareTo(MFA_THRESHOLD) > 0) {
            throw new TransactionException("MFA required for transactions exceeding $10,000");
        }

        String referenceId = generateReferenceId(accountNumber, amount);
        return new TransactionResult(referenceId, "DEBIT", amount, "SUCCESS");
    }

    /**
     * Processes a credit transaction to a customer account.
     * OCC compliance path — must have test coverage.
     *
     * @param accountNumber  PII field — customer account number
     * @param amount         transaction amount in USD
     * @param requestingUser the authenticated user initiating the transaction
     * @return TransactionResult with status and reference ID
     */
    public TransactionResult processCredit(String accountNumber, BigDecimal amount,
                                           String requestingUser) {
        // PII field — mask before logging
        validateAccountNumber(accountNumber);
        validateAmount(amount);

        if (requestingUser == null || requestingUser.isBlank()) {
            throw new TransactionException("Requesting user must not be null for credit transactions");
        }

        String referenceId = generateReferenceId(accountNumber, amount);
        return new TransactionResult(referenceId, "CREDIT", amount, "SUCCESS");
    }

    /**
     * Validates whether a transaction amount is within policy limits.
     * OCC compliance path — amount validation is a compliance-critical check.
     *
     * @param amount the transaction amount
     * @throws TransactionException if amount is null, negative, zero, or exceeds daily limit
     */
    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new TransactionException("Transaction amount must not be null");
        }
        if (amount.compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            throw new TransactionException("Transaction amount must be at least $0.01");
        }
        if (amount.compareTo(DAILY_LIMIT) > 0) {
            throw new TransactionException("Transaction amount exceeds daily limit of $50,000");
        }
    }

    // -- Private validation helpers --

    private void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new TransactionException("Account number must not be null or empty");
        }
        if (!accountNumber.matches("\\d{10,12}")) {
            throw new TransactionException("Account number must be 10-12 digits");
        }
    }

    private void validateRoutingNumber(String routingNumber) {
        if (routingNumber == null || routingNumber.isBlank()) {
            throw new TransactionException("Routing number must not be null or empty");
        }
        if (!routingNumber.matches("\\d{9}")) {
            throw new TransactionException("Routing number must be exactly 9 digits");
        }
    }

    private void validateDailyLimit(String accountNumber, BigDecimal amount) {
        // Simulated daily total lookup — in production queries transaction history
        BigDecimal dailyTotal = getDailyTotal(accountNumber);
        if (dailyTotal.add(amount).compareTo(DAILY_LIMIT) > 0) {
            throw new TransactionException("Transaction would exceed daily limit of $50,000");
        }
    }

    private BigDecimal getDailyTotal(String accountNumber) {
        // Simulated — in production queries TransactionRepository
        return BigDecimal.ZERO;
    }

    private String generateReferenceId(String accountNumber, BigDecimal amount) {
        return "TXN-" + System.currentTimeMillis();
    }

    // -- Inner classes --

    public static class TransactionResult {
        public final String referenceId;
        public final String type;
        public final BigDecimal amount;
        public final String status;

        public TransactionResult(String referenceId, String type, BigDecimal amount, String status) {
            this.referenceId = referenceId;
            this.type = type;
            this.amount = amount;
            this.status = status;
        }
    }

    public static class TransactionException extends RuntimeException {
        public TransactionException(String message) {
            super(message);
        }
    }
}
