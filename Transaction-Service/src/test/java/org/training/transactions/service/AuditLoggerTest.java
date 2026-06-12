package org.training.transactions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.training.transactions.service.AuditLogger.AuditException;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    private AuditLogger auditLogger;

    private static final String VALID_REF   = "TXN-001";
    private static final String VALID_TYPE  = "DEBIT";
    private static final String VALID_ACCT  = "1234567890";  // 10 digits
    private static final String VALID_USER  = "teller01";

    @BeforeEach
    void setUp() {
        auditLogger = new AuditLogger();
    }

    // ── logTransactionSuccess – happy path ──────────────────────────────

    @Test
    void should_logSuccessfully_when_allFieldsValid() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionSuccess(VALID_REF, VALID_TYPE, VALID_ACCT, VALID_USER));
    }

    // ── logTransactionSuccess – null/blank required fields ──────────────

    @Test
    void should_throwAuditException_when_successReferenceIdIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(null, VALID_TYPE, VALID_ACCT, VALID_USER));
        assertTrue(ex.getMessage().contains("Reference ID"));
    }

    @Test
    void should_throwAuditException_when_successReferenceIdIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess("  ", VALID_TYPE, VALID_ACCT, VALID_USER));
        assertTrue(ex.getMessage().contains("Reference ID"));
    }

    @Test
    void should_throwAuditException_when_successTransactionTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, null, VALID_ACCT, VALID_USER));
        assertTrue(ex.getMessage().contains("Transaction type"));
    }

    @Test
    void should_throwAuditException_when_successTransactionTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, "  ", VALID_ACCT, VALID_USER));
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
                () -> auditLogger.logTransactionSuccess(VALID_REF, VALID_TYPE, VALID_ACCT, null));
        assertTrue(ex.getMessage().contains("Requesting user"));
    }

    @Test
    void should_throwAuditException_when_successRequestingUserIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionSuccess(VALID_REF, VALID_TYPE, VALID_ACCT, "  "));
        assertTrue(ex.getMessage().contains("Requesting user"));
    }

    // ── logTransactionFailure – happy path ──────────────────────────────

    @Test
    void should_logFailureSuccessfully_when_allFieldsValid() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, VALID_ACCT, VALID_USER, "Insufficient funds"));
    }

    @Test
    void should_logFailure_when_referenceIdIsNull() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(null, VALID_TYPE, VALID_ACCT, VALID_USER, "Pre-validation failure"));
    }

    @Test
    void should_logFailure_when_accountNumberIsNull() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, null, VALID_USER, "Unknown account"));
    }

    @Test
    void should_logFailure_when_requestingUserIsNull() {
        assertDoesNotThrow(() ->
                auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, VALID_ACCT, null, "System error"));
    }

    // ── logTransactionFailure – required fields ─────────────────────────

    @Test
    void should_throwAuditException_when_failureTransactionTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF, null, VALID_ACCT, VALID_USER, "reason"));
        assertTrue(ex.getMessage().contains("Transaction type"));
    }

    @Test
    void should_throwAuditException_when_failureTransactionTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF, "  ", VALID_ACCT, VALID_USER, "reason"));
        assertTrue(ex.getMessage().contains("Transaction type"));
    }

    @Test
    void should_throwAuditException_when_failureReasonIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, VALID_ACCT, VALID_USER, null));
        assertTrue(ex.getMessage().contains("Failure reason"));
    }

    @Test
    void should_throwAuditException_when_failureReasonIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logTransactionFailure(VALID_REF, VALID_TYPE, VALID_ACCT, VALID_USER, "  "));
        assertTrue(ex.getMessage().contains("Failure reason"));
    }

    // ── logAuthEvent – happy path ───────────────────────────────────────

    @Test
    void should_logAuthEvent_when_allFieldsValid() {
        assertDoesNotThrow(() ->
                auditLogger.logAuthEvent("user1", "LOGIN_SUCCESS", "192.168.1.1"));
    }

    @Test
    void should_logAuthEvent_when_ipAddressIsNull() {
        assertDoesNotThrow(() ->
                auditLogger.logAuthEvent("user1", "MFA_FAILURE", null));
    }

    // ── logAuthEvent – required fields ──────────────────────────────────

    @Test
    void should_throwAuditException_when_authUserIdIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent(null, "LOGIN_SUCCESS", "1.2.3.4"));
        assertTrue(ex.getMessage().contains("User ID"));
    }

    @Test
    void should_throwAuditException_when_authUserIdIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("  ", "LOGIN_SUCCESS", "1.2.3.4"));
        assertTrue(ex.getMessage().contains("User ID"));
    }

    @Test
    void should_throwAuditException_when_authEventTypeIsNull() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("user1", null, "1.2.3.4"));
        assertTrue(ex.getMessage().contains("Event type"));
    }

    @Test
    void should_throwAuditException_when_authEventTypeIsBlank() {
        AuditException ex = assertThrows(AuditException.class,
                () -> auditLogger.logAuthEvent("user1", "  ", "1.2.3.4"));
        assertTrue(ex.getMessage().contains("Event type"));
    }
}
