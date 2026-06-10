package org.training.transactions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.training.transactions.service.AuditLogger.AuditException;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    private AuditLogger auditLogger;

    // Valid test fixtures — account must be >= 4 chars for PIIDataHandler.maskAccountNumber
    private static final String VALID_REF = "TXN-1234567890";
    private static final String VALID_TYPE = "DEBIT";
    private static final String VALID_ACCOUNT = "1234567890";
    private static final String VALID_USER = "teller_jane";

    @BeforeEach
    void setUp() {
        auditLogger = new AuditLogger();
    }

    // ── logTransactionSuccess — happy path ──────────────────────────────

    @Test
    void should_logSuccess_when_allFieldsValid() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionSuccess(VALID_REF, VALID_TYPE, VALID_ACCOUNT, VALID_USER));
    }

    // ── logTransactionSuccess — null/blank required fields ──────────────

    @Test
    void should_throwAuditException_when_successReferenceIdIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(null, VALID_TYPE, VALID_ACCOUNT, VALID_USER));
        assertTrue(ex.getMessage().contains("Reference ID"));
    }

    @Test
    void should_throwAuditException_when_successReferenceIdIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess("  ", VALID_TYPE, VALID_ACCOUNT, VALID_USER));
        assertTrue(ex.getMessage().contains("Reference ID"));
    }

    @Test
    void should_throwAuditException_when_successTransactionTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, null, VALID_ACCOUNT, VALID_USER));
        assertTrue(ex.getMessage().contains("Transaction type"));
    }

    @Test
    void should_throwAuditException_when_successTransactionTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, " ", VALID_ACCOUNT, VALID_USER));
        assertTrue(ex.getMessage().contains("Transaction type"));
    }

    @Test
    void should_throwAuditException_when_successAccountNumberIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, VALID_TYPE, null, VALID_USER));
        assertTrue(ex.getMessage().contains("Account number"));
    }

    @Test
    void should_throwAuditException_when_successAccountNumberIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, VALID_TYPE, "  ", VALID_USER));
        assertTrue(ex.getMessage().contains("Account number"));
    }

    @Test
    void should_throwAuditException_when_successRequestingUserIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, VALID_TYPE, VALID_ACCOUNT, null));
        assertTrue(ex.getMessage().contains("Requesting user"));
    }

    @Test
    void should_throwAuditException_when_successRequestingUserIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, VALID_TYPE, VALID_ACCOUNT, " "));
        assertTrue(ex.getMessage().contains("Requesting user"));
    }

    // ── logTransactionFailure — happy path ──────────────────────────────

    @Test
    void should_logFailure_when_allFieldsValid() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, VALID_ACCOUNT,
                        VALID_USER, "Insufficient funds"));
    }

    @Test
    void should_logFailure_when_referenceIdIsNull() {
        // referenceId defaults to "N/A"
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(null, VALID_TYPE, VALID_ACCOUNT,
                        VALID_USER, "Insufficient funds"));
    }

    @Test
    void should_logFailure_when_accountNumberIsNull() {
        // accountNumber defaults to "UNKNOWN"
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, null,
                        VALID_USER, "Insufficient funds"));
    }

    @Test
    void should_logFailure_when_requestingUserIsNull() {
        // requestingUser defaults to "UNKNOWN"
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, VALID_ACCOUNT,
                        null, "Insufficient funds"));
    }

    // ── logTransactionFailure — required fields ─────────────────────────

    @Test
    void should_throwAuditException_when_failureTransactionTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF, null, VALID_ACCOUNT,
                        VALID_USER, "Insufficient funds"));
        assertTrue(ex.getMessage().contains("Transaction type"));
    }

    @Test
    void should_throwAuditException_when_failureTransactionTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF, " ", VALID_ACCOUNT,
                        VALID_USER, "Insufficient funds"));
        assertTrue(ex.getMessage().contains("Transaction type"));
    }

    @Test
    void should_throwAuditException_when_failureReasonIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, VALID_ACCOUNT,
                        VALID_USER, null));
        assertTrue(ex.getMessage().contains("Failure reason"));
    }

    @Test
    void should_throwAuditException_when_failureReasonIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, VALID_ACCOUNT,
                        VALID_USER, "  "));
        assertTrue(ex.getMessage().contains("Failure reason"));
    }

    // ── logAuthEvent — happy path ───────────────────────────────────────

    @Test
    void should_logAuthEvent_when_allFieldsValid() {
        assertDoesNotThrow(() ->
                auditLogger.logAuthEvent("user1", "LOGIN_SUCCESS", "192.168.1.1"));
    }

    @Test
    void should_logAuthEvent_when_ipAddressIsNull() {
        // ipAddress defaults to "UNKNOWN"
        assertDoesNotThrow(() ->
                auditLogger.logAuthEvent("user1", "LOGIN_SUCCESS", null));
    }

    // ── logAuthEvent — required fields ──────────────────────────────────

    @Test
    void should_throwAuditException_when_authUserIdIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent(null, "LOGIN_SUCCESS", "192.168.1.1"));
        assertTrue(ex.getMessage().contains("User ID"));
    }

    @Test
    void should_throwAuditException_when_authUserIdIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("  ", "LOGIN_SUCCESS", "192.168.1.1"));
        assertTrue(ex.getMessage().contains("User ID"));
    }

    @Test
    void should_throwAuditException_when_authEventTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("user1", null, "192.168.1.1"));
        assertTrue(ex.getMessage().contains("Event type"));
    }

    @Test
    void should_throwAuditException_when_authEventTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("user1", "  ", "192.168.1.1"));
        assertTrue(ex.getMessage().contains("Event type"));
    }
}
