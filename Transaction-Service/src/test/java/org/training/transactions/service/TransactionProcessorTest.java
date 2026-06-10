package org.training.transactions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.training.transactions.service.TransactionProcessor.TransactionException;
import org.training.transactions.service.TransactionProcessor.TransactionResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionProcessorTest {

    private TransactionProcessor processor;

    // Valid test fixtures
    private static final String VALID_ACCOUNT_10 = "1234567890";
    private static final String VALID_ACCOUNT_12 = "123456789012";
    private static final String VALID_ROUTING = "123456789";
    private static final String VALID_USER = "teller_jane";

    @BeforeEach
    void setUp() {
        processor = new TransactionProcessor();
    }

    // ── processDebit — happy path ───────────────────────────────────────

    @Test
    void should_returnDebitResult_when_allFieldsValid() {
        TransactionResult result = processor.processDebit(
                VALID_ACCOUNT_10, VALID_ROUTING, new BigDecimal("500.00"), VALID_USER);

        assertTrue(result.referenceId.startsWith("TXN-"));
        assertEquals("DEBIT", result.type);
        assertEquals(new BigDecimal("500.00"), result.amount);
        assertEquals("SUCCESS", result.status);
    }

    @Test
    void should_returnDebitResult_when_accountIs12Digits() {
        TransactionResult result = processor.processDebit(
                VALID_ACCOUNT_12, VALID_ROUTING, new BigDecimal("100.00"), VALID_USER);
        assertEquals("DEBIT", result.type);
    }

    @Test
    void should_returnDebitResult_when_amountIsMinimum() {
        TransactionResult result = processor.processDebit(
                VALID_ACCOUNT_10, VALID_ROUTING, new BigDecimal("0.01"), VALID_USER);
        assertEquals("SUCCESS", result.status);
    }

    @Test
    void should_returnDebitResult_when_amountIsAtMfaThreshold() {
        // $10,000.00 exactly — compareTo(MFA_THRESHOLD) == 0, should NOT throw
        TransactionResult result = processor.processDebit(
                VALID_ACCOUNT_10, VALID_ROUTING, new BigDecimal("10000.00"), VALID_USER);
        assertEquals("SUCCESS", result.status);
    }

    // ── processDebit — requestingUser not validated (production gap) ────

    @Test
    void should_succeedDebit_when_requestingUserIsNull() {
        // Documents production gap: processDebit does not validate requestingUser
        // unlike processCredit which rejects null/blank. OCC "who" field is missing.
        TransactionResult result = processor.processDebit(
                VALID_ACCOUNT_10, VALID_ROUTING, new BigDecimal("100.00"), null);
        assertEquals("SUCCESS", result.status);
    }

    @Test
    void should_succeedDebit_when_requestingUserIsBlank() {
        // Documents production gap: processDebit does not validate requestingUser
        TransactionResult result = processor.processDebit(
                VALID_ACCOUNT_10, VALID_ROUTING, new BigDecimal("100.00"), "  ");
        assertEquals("SUCCESS", result.status);
    }

    // ── processDebit — MFA threshold ────────────────────────────────────

    @Test
    void should_throwTransactionException_when_amountExceedsMfaThreshold() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, VALID_ROUTING,
                        new BigDecimal("10000.01"), VALID_USER));
        assertTrue(ex.getMessage().contains("MFA required"));
    }

    // ── processDebit — account validation ───────────────────────────────

    @Test
    void should_throwTransactionException_when_debitAccountIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(null, VALID_ROUTING,
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number"));
    }

    @Test
    void should_throwTransactionException_when_debitAccountIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("  ", VALID_ROUTING,
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number"));
    }

    @Test
    void should_throwTransactionException_when_debitAccountTooShort() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("12345", VALID_ROUTING,
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    @Test
    void should_throwTransactionException_when_debitAccountTooLong() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("1234567890123", VALID_ROUTING,
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    @Test
    void should_throwTransactionException_when_debitAccountNonNumeric() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("12345abcde", VALID_ROUTING,
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    // ── processDebit — routing validation ───────────────────────────────

    @Test
    void should_throwTransactionException_when_routingIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, null,
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Routing number"));
    }

    @Test
    void should_throwTransactionException_when_routingIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, "  ",
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Routing number"));
    }

    @Test
    void should_throwTransactionException_when_routingIs8Digits() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, "12345678",
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwTransactionException_when_routingIs10Digits() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, "1234567890",
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwTransactionException_when_routingIsNonNumeric() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, "12345abcd",
                        new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    // ── processCredit — happy path ──────────────────────────────────────

    @Test
    void should_returnCreditResult_when_allFieldsValid() {
        TransactionResult result = processor.processCredit(
                VALID_ACCOUNT_10, new BigDecimal("250.00"), VALID_USER);

        assertTrue(result.referenceId.startsWith("TXN-"));
        assertEquals("CREDIT", result.type);
        assertEquals(new BigDecimal("250.00"), result.amount);
        assertEquals("SUCCESS", result.status);
    }

    @Test
    void should_returnCreditResult_when_amountIsAtDailyLimit() {
        TransactionResult result = processor.processCredit(
                VALID_ACCOUNT_10, new BigDecimal("50000.00"), VALID_USER);
        assertEquals("SUCCESS", result.status);
    }

    // ── processCredit — requestingUser null/blank ───────────────────────

    @Test
    void should_throwTransactionException_when_creditUserIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(VALID_ACCOUNT_10,
                        new BigDecimal("100.00"), null));
        assertTrue(ex.getMessage().contains("Requesting user"));
    }

    @Test
    void should_throwTransactionException_when_creditUserIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(VALID_ACCOUNT_10,
                        new BigDecimal("100.00"), "  "));
        assertTrue(ex.getMessage().contains("Requesting user"));
    }

    // ── processCredit — account validation (reuses same private method) ─

    @Test
    void should_throwTransactionException_when_creditAccountIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(null, new BigDecimal("100.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number"));
    }

    // ── validateAmount — standalone ─────────────────────────────────────

    @Test
    void should_throwTransactionException_when_amountIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.validateAmount(null));
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    void should_throwTransactionException_when_amountIsZero() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.validateAmount(BigDecimal.ZERO));
        assertTrue(ex.getMessage().contains("at least $0.01"));
    }

    @Test
    void should_throwTransactionException_when_amountIsNegative() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.validateAmount(new BigDecimal("-1.00")));
        assertTrue(ex.getMessage().contains("at least $0.01"));
    }

    @Test
    void should_pass_when_amountIsMinValid() {
        assertDoesNotThrow(() -> processor.validateAmount(new BigDecimal("0.01")));
    }

    @Test
    void should_pass_when_amountIsAtDailyLimit() {
        assertDoesNotThrow(() -> processor.validateAmount(new BigDecimal("50000.00")));
    }

    @Test
    void should_throwTransactionException_when_amountExceedsDailyLimit() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.validateAmount(new BigDecimal("50000.01")));
        assertTrue(ex.getMessage().contains("exceeds daily limit"));
    }
}
