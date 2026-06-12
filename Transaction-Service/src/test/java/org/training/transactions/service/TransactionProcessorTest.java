package org.training.transactions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.training.transactions.service.TransactionProcessor.TransactionException;
import org.training.transactions.service.TransactionProcessor.TransactionResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionProcessorTest {

    private TransactionProcessor processor;

    private static final String VALID_ACCOUNT = "1234567890";   // 10 digits
    private static final String VALID_ROUTING = "123456789";    // 9 digits
    private static final String VALID_USER    = "teller01";

    @BeforeEach
    void setUp() {
        processor = new TransactionProcessor();
    }

    // ── processDebit – happy path ───────────────────────────────────────

    @Test
    void should_returnDebitResult_when_allFieldsValid() {
        TransactionResult r = processor.processDebit(
                VALID_ACCOUNT, VALID_ROUTING, new BigDecimal("100.00"), VALID_USER);
        assertEquals("DEBIT", r.type);
        assertEquals("SUCCESS", r.status);
        assertTrue(r.referenceId.startsWith("TXN-"));
        assertEquals(0, new BigDecimal("100.00").compareTo(r.amount));
    }

    @Test
    void should_returnDebitResult_when_amountAtMFAThreshold() {
        TransactionResult r = processor.processDebit(
                VALID_ACCOUNT, VALID_ROUTING, new BigDecimal("10000.00"), VALID_USER);
        assertEquals("DEBIT", r.type);
    }

    @Test
    void should_returnDebitResult_when_accountIs12Digits() {
        TransactionResult r = processor.processDebit(
                "123456789012", VALID_ROUTING, new BigDecimal("1.00"), VALID_USER);
        assertEquals("DEBIT", r.type);
    }

    // ── processDebit – MFA threshold ────────────────────────────────────

    @Test
    void should_throwTransactionException_when_amountAboveMFAThreshold() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT, VALID_ROUTING,
                        new BigDecimal("10000.01"), VALID_USER));
        assertTrue(ex.getMessage().contains("MFA required"));
    }

    // ── processDebit – account validation ───────────────────────────────

    @Test
    void should_throwTransactionException_when_accountIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(null, VALID_ROUTING,
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number"));
    }

    @Test
    void should_throwTransactionException_when_accountIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("  ", VALID_ROUTING,
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number"));
    }

    @Test
    void should_throwTransactionException_when_accountTooShort() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("12345", VALID_ROUTING,
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    @Test
    void should_throwTransactionException_when_accountTooLong() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("1234567890123", VALID_ROUTING,
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    @Test
    void should_throwTransactionException_when_accountNonNumeric() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("abcdefghij", VALID_ROUTING,
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    // ── processDebit – routing validation ───────────────────────────────

    @Test
    void should_throwTransactionException_when_routingIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT, null,
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Routing number"));
    }

    @Test
    void should_throwTransactionException_when_routingIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT, "  ",
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Routing number"));
    }

    @Test
    void should_throwTransactionException_when_routingTooShort() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT, "12345678",
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwTransactionException_when_routingTooLong() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT, "1234567890",
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwTransactionException_when_routingNonNumeric() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT, "abcdefghi",
                        new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    // ── processCredit – happy path ──────────────────────────────────────

    @Test
    void should_returnCreditResult_when_allFieldsValid() {
        TransactionResult r = processor.processCredit(
                VALID_ACCOUNT, new BigDecimal("250.00"), VALID_USER);
        assertEquals("CREDIT", r.type);
        assertEquals("SUCCESS", r.status);
        assertTrue(r.referenceId.startsWith("TXN-"));
    }

    @Test
    void should_returnCreditResult_when_amountAtDailyLimit() {
        TransactionResult r = processor.processCredit(
                VALID_ACCOUNT, new BigDecimal("50000.00"), VALID_USER);
        assertEquals("CREDIT", r.type);
    }

    // ── processCredit – requestingUser validation ───────────────────────

    @Test
    void should_throwTransactionException_when_creditUserIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(VALID_ACCOUNT, new BigDecimal("1.00"), null));
        assertTrue(ex.getMessage().contains("Requesting user"));
    }

    @Test
    void should_throwTransactionException_when_creditUserIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(VALID_ACCOUNT, new BigDecimal("1.00"), "  "));
        assertTrue(ex.getMessage().contains("Requesting user"));
    }

    // ── processCredit – account validation (same rules) ─────────────────

    @Test
    void should_throwTransactionException_when_creditAccountIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(null, new BigDecimal("1.00"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number"));
    }

    // ── validateAmount ──────────────────────────────────────────────────

    @Test
    void should_pass_when_amountIsMinimum() {
        assertDoesNotThrow(() -> processor.validateAmount(new BigDecimal("0.01")));
    }

    @Test
    void should_pass_when_amountIsAtDailyLimit() {
        assertDoesNotThrow(() -> processor.validateAmount(new BigDecimal("50000.00")));
    }

    @Test
    void should_throwTransactionException_when_amountIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.validateAmount(null));
        assertTrue(ex.getMessage().contains("null"));
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
                () -> processor.validateAmount(new BigDecimal("-5.00")));
        assertTrue(ex.getMessage().contains("at least $0.01"));
    }

    @Test
    void should_throwTransactionException_when_amountExceedsDailyLimit() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.validateAmount(new BigDecimal("50000.01")));
        assertTrue(ex.getMessage().contains("exceeds daily limit"));
    }
}
