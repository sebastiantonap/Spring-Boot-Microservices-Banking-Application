package org.training.transactions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Instant;

/**
 * AuditLogger - Compliance event logging for all transaction operations.
 * OCC compliance path: All state-changing operations must produce an immutable audit record.
 * Audit records must include: who, what, when, and outcome.
 * PII fields must be masked before logging.
 */
@Service
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    /**
     * Logs a successful transaction event to the compliance audit trail.
     * OCC compliance path — must have test coverage.
     *
     * @param referenceId    the unique transaction reference ID
     * @param transactionType DEBIT, CREDIT, or TRANSFER
     * @param accountNumber  PII field — must be masked before logging
     * @param requestingUser the authenticated user who initiated the transaction
     * @throws AuditException if any required field is missing
     */
    public void logTransactionSuccess(String referenceId, String transactionType,
                                      String accountNumber, String requestingUser) {
        validateAuditFields(referenceId, transactionType, accountNumber, requestingUser);

        // PII field — mask before logging
        String maskedAccount = PIIDataHandler.maskAccountNumber(accountNumber);

        AuditEvent event = new AuditEvent(
                referenceId,
                transactionType,
                maskedAccount,
                requestingUser,
                "SUCCESS",
                Instant.now()
        );

        log.info("[AUDIT] {}", event);
    }

    /**
     * Logs a failed transaction event to the compliance audit trail.
     * OCC compliance path — failed attempts must be logged for fraud detection.
     *
     * @param referenceId    the unique transaction reference ID (may be null if pre-validation failure)
     * @param transactionType DEBIT, CREDIT, or TRANSFER
     * @param accountNumber  PII field — must be masked before logging
     * @param requestingUser the authenticated user who attempted the transaction
     * @param failureReason  human-readable reason for the failure
     */
    public void logTransactionFailure(String referenceId, String transactionType,
                                      String accountNumber, String requestingUser,
                                      String failureReason) {
        if (transactionType == null || transactionType.isBlank()) {
            throw new AuditException("Transaction type must not be null for failure audit log");
        }
        if (failureReason == null || failureReason.isBlank()) {
            throw new AuditException("Failure reason must not be null for failure audit log");
        }

        // PII field — mask before logging
        String maskedAccount = accountNumber != null
                ? PIIDataHandler.maskAccountNumber(accountNumber)
                : "UNKNOWN";

        AuditEvent event = new AuditEvent(
                referenceId != null ? referenceId : "N/A",
                transactionType,
                maskedAccount,
                requestingUser != null ? requestingUser : "UNKNOWN",
                "FAILURE: " + failureReason,
                Instant.now()
        );

        log.warn("[AUDIT] {}", event);
    }

    /**
     * Logs an authentication event (login, MFA challenge, session expiry).
     * OCC compliance path — all authentication events must be auditable.
     *
     * @param userId    the user attempting authentication
     * @param eventType LOGIN_SUCCESS, LOGIN_FAILURE, MFA_SUCCESS, MFA_FAILURE, SESSION_EXPIRED
     * @param ipAddress the originating IP address
     */
    public void logAuthEvent(String userId, String eventType, String ipAddress) {
        if (userId == null || userId.isBlank()) {
            throw new AuditException("User ID must not be null for auth audit log");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new AuditException("Event type must not be null for auth audit log");
        }

        log.info("[AUTH AUDIT] user={} event={} ip={} timestamp={}",
                userId, eventType, ipAddress != null ? ipAddress : "UNKNOWN", Instant.now());
    }

    // -- Private helpers --

    private void validateAuditFields(String referenceId, String transactionType,
                                     String accountNumber, String requestingUser) {
        if (referenceId == null || referenceId.isBlank()) {
            throw new AuditException("Reference ID must not be null for audit log");
        }
        if (transactionType == null || transactionType.isBlank()) {
            throw new AuditException("Transaction type must not be null for audit log");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new AuditException("Account number must not be null for audit log");
        }
        if (requestingUser == null || requestingUser.isBlank()) {
            throw new AuditException("Requesting user must not be null for audit log");
        }
    }

    // -- Inner classes --

    public static class AuditEvent {
        public final String referenceId;
        public final String transactionType;
        public final String maskedAccount;
        public final String requestingUser;
        public final String outcome;
        public final Instant timestamp;

        public AuditEvent(String referenceId, String transactionType, String maskedAccount,
                          String requestingUser, String outcome, Instant timestamp) {
            this.referenceId = referenceId;
            this.transactionType = transactionType;
            this.maskedAccount = maskedAccount;
            this.requestingUser = requestingUser;
            this.outcome = outcome;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("referenceId=%s type=%s account=%s user=%s outcome=%s timestamp=%s",
                    referenceId, transactionType, maskedAccount, requestingUser, outcome, timestamp);
        }
    }

    public static class AuditException extends RuntimeException {
        public AuditException(String message) {
            super(message);
        }
    }
}
