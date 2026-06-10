package org.training.transactions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.training.transactions.service.AuditLogger.AuditException;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    private AuditLogger auditLogger;

    private static final String VALID_REF_ID = "TXN-1234567890";
    private static final String VALID_TYPE = "DEBIT";
    private static final String VALID_ACCOUNT = "123456789012";
    private static final String VALID_USER = "teller1";

    @BeforeEach
    void setUp() {
        auditLogger = new AuditLogger();
    }

    // ===== logTransactionSuccess =====

    @Test
    void should_throwAuditException_when_successReferenceIdIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(null, VALID_TYPE, VALID_ACCOUNT, VALID_USER));
        assertTrue(ex.getMessage().contains("Reference ID must not be null"));
    }

    @Test
    void should_throwAuditException_when_successReferenceIdIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess("  ", VALID_TYPE, VALID_ACCOUNT, VALID_USER));
        assertTrue(ex.getMessage().contains("Reference ID must not be null"));
    }

    @Test
    void should_throwAuditException_when_successTransactionTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF_ID, null, VALID_ACCOUNT, VALID_USER));
        assertTrue(ex.getMessage().contains("Transaction type must not be null"));
    }

    @Test
    void should_throwAuditException_when_successTransactionTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF_ID, "  ", VALID_ACCOUNT, VALID_USER));
        assertTrue(ex.getMessage().contains("Transaction type must not be null"));
    }

    @Test
    void should_throwAuditException_when_successAccountNumberIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF_ID, VALID_TYPE, null, VALID_USER));
        assertTrue(ex.getMessage().contains("Account number must not be null"));
    }

    @Test
    void should_throwAuditException_when_successAccountNumberIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF_ID, VALID_TYPE, "  ", VALID_USER));
        assertTrue(ex.getMessage().contains("Account number must not be null"));
    }

    @Test
    void should_throwAuditException_when_successRequestingUserIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF_ID, VALID_TYPE, VALID_ACCOUNT, null));
        assertTrue(ex.getMessage().contains("Requesting user must not be null"));
    }

    @Test
    void should_throwAuditException_when_successRequestingUserIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF_ID, VALID_TYPE, VALID_ACCOUNT, "  "));
        assertTrue(ex.getMessage().contains("Requesting user must not be null"));
    }

    @Test
    void should_logSuccessfully_when_allFieldsValid() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionSuccess(VALID_REF_ID, VALID_TYPE, VALID_ACCOUNT, VALID_USER));
    }

    // ===== logTransactionFailure =====

    @Test
    void should_throwAuditException_when_failureTransactionTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF_ID, null, VALID_ACCOUNT, VALID_USER, "Insufficient funds"));
        assertTrue(ex.getMessage().contains("Transaction type must not be null"));
    }

    @Test
    void should_throwAuditException_when_failureTransactionTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF_ID, "  ", VALID_ACCOUNT, VALID_USER, "Insufficient funds"));
        assertTrue(ex.getMessage().contains("Transaction type must not be null"));
    }

    @Test
    void should_throwAuditException_when_failureReasonIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF_ID, VALID_TYPE, VALID_ACCOUNT, VALID_USER, null));
        assertTrue(ex.getMessage().contains("Failure reason must not be null"));
    }

    @Test
    void should_throwAuditException_when_failureReasonIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF_ID, VALID_TYPE, VALID_ACCOUNT, VALID_USER, "  "));
        assertTrue(ex.getMessage().contains("Failure reason must not be null"));
    }

    @Test
    void should_logFailure_when_referenceIdIsNull() {
        // referenceId defaults to "N/A" when null
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(null, VALID_TYPE, VALID_ACCOUNT, VALID_USER, "Timeout"));
    }

    @Test
    void should_logFailure_when_accountNumberIsNull() {
        // accountNumber defaults to "UNKNOWN" when null
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF_ID, VALID_TYPE, null, VALID_USER, "Validation error"));
    }

    @Test
    void should_logFailure_when_requestingUserIsNull() {
        // requestingUser defaults to "UNKNOWN" when null
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF_ID, VALID_TYPE, VALID_ACCOUNT, null, "Auth failed"));
    }

    @Test
    void should_logFailure_when_allFieldsValid() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF_ID, VALID_TYPE, VALID_ACCOUNT, VALID_USER, "Exceeded limit"));
    }

    // ===== logAuthEvent =====

    @Test
    void should_throwAuditException_when_authEventUserIdIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent(null, "LOGIN_SUCCESS", "10.0.0.1"));
        assertTrue(ex.getMessage().contains("User ID must not be null"));
    }

    @Test
    void should_throwAuditException_when_authEventUserIdIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("  ", "LOGIN_SUCCESS", "10.0.0.1"));
        assertTrue(ex.getMessage().contains("User ID must not be null"));
    }

    @Test
    void should_throwAuditException_when_authEventTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("user1", null, "10.0.0.1"));
        assertTrue(ex.getMessage().contains("Event type must not be null"));
    }

    @Test
    void should_throwAuditException_when_authEventTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("user1", "  ", "10.0.0.1"));
        assertTrue(ex.getMessage().contains("Event type must not be null"));
    }

    @Test
    void should_logAuthEvent_when_ipAddressIsNull() {
        // ipAddress defaults to "UNKNOWN" when null
        assertDoesNotThrow(() -> auditLogger.logAuthEvent("user1", "LOGIN_SUCCESS", null));
    }

    @Test
    void should_logAuthEvent_when_allFieldsValid() {
        assertDoesNotThrow(() -> auditLogger.logAuthEvent("user1", "MFA_SUCCESS", "192.168.1.1"));
    }
}
