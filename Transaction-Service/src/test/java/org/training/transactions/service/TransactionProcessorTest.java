package org.training.transactions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.training.transactions.service.TransactionProcessor.TransactionException;
import org.training.transactions.service.TransactionProcessor.TransactionResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionProcessorTest {

    private TransactionProcessor processor;

    private static final String VALID_ACCOUNT_10 = "1234567890";
    private static final String VALID_ACCOUNT_12 = "123456789012";
    private static final String VALID_ROUTING = "123456789";
    private static final String VALID_USER = "teller1";

    @BeforeEach
    void setUp() {
        processor = new TransactionProcessor();
    }

    // ===== validateAmount =====

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

    // ===== processDebit =====

    @Test
    void should_throwTransactionException_when_debitAccountIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(null, VALID_ROUTING, new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number must not be null"));
    }

    @Test
    void should_throwTransactionException_when_debitAccountIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("  ", VALID_ROUTING, new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number must not be null"));
    }

    @Test
    void should_throwTransactionException_when_debitAccountTooShort() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("12345", VALID_ROUTING, new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    @Test
    void should_throwTransactionException_when_debitAccountTooLong() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("1234567890123", VALID_ROUTING, new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    @Test
    void should_throwTransactionException_when_debitAccountNonNumeric() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit("12345abcde", VALID_ROUTING, new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    @Test
    void should_throwTransactionException_when_routingIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, null, new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("Routing number must not be null"));
    }

    @Test
    void should_throwTransactionException_when_routingIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, "  ", new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("Routing number must not be null"));
    }

    @Test
    void should_throwTransactionException_when_routingHas8Digits() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, "12345678", new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwTransactionException_when_routingHas10Digits() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, "1234567890", new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwTransactionException_when_routingIsNonNumeric() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, "12345abcd", new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwTransactionException_when_debitAmountExceedsMFAThreshold() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processDebit(VALID_ACCOUNT_10, VALID_ROUTING, new BigDecimal("10000.01"), VALID_USER));
        assertTrue(ex.getMessage().contains("MFA required"));
    }

    @Test
    void should_processDebit_when_amountAtMFAThreshold() {
        TransactionResult result = processor.processDebit(VALID_ACCOUNT_10, VALID_ROUTING,
                new BigDecimal("10000.00"), VALID_USER);
        assertNotNull(result);
        assertEquals("DEBIT", result.type);
        assertEquals("SUCCESS", result.status);
        assertTrue(result.referenceId.startsWith("TXN-"));
    }

    @Test
    void should_processDebit_when_allFieldsValid() {
        TransactionResult result = processor.processDebit(VALID_ACCOUNT_12, VALID_ROUTING,
                new BigDecimal("500.00"), VALID_USER);
        assertNotNull(result);
        assertEquals("DEBIT", result.type);
        assertEquals(new BigDecimal("500.00"), result.amount);
        assertEquals("SUCCESS", result.status);
        assertTrue(result.referenceId.startsWith("TXN-"));
    }

    @Test
    void should_processDebit_when_amountIsMinimum() {
        TransactionResult result = processor.processDebit(VALID_ACCOUNT_10, VALID_ROUTING,
                new BigDecimal("0.01"), VALID_USER);
        assertNotNull(result);
        assertEquals("DEBIT", result.type);
    }

    // ===== processCredit =====

    @Test
    void should_throwTransactionException_when_creditAccountIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(null, new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("Account number must not be null"));
    }

    @Test
    void should_throwTransactionException_when_creditAccountTooShort() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit("12345", new BigDecimal("100"), VALID_USER));
        assertTrue(ex.getMessage().contains("10-12 digits"));
    }

    @Test
    void should_throwTransactionException_when_creditRequestingUserIsNull() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(VALID_ACCOUNT_10, new BigDecimal("100"), null));
        assertTrue(ex.getMessage().contains("Requesting user must not be null"));
    }

    @Test
    void should_throwTransactionException_when_creditRequestingUserIsBlank() {
        TransactionException ex = assertThrows(TransactionException.class,
                () -> processor.processCredit(VALID_ACCOUNT_10, new BigDecimal("100"), "  "));
        assertTrue(ex.getMessage().contains("Requesting user must not be null"));
    }

    @Test
    void should_processCredit_when_allFieldsValid() {
        TransactionResult result = processor.processCredit(VALID_ACCOUNT_10,
                new BigDecimal("2500.00"), VALID_USER);
        assertNotNull(result);
        assertEquals("CREDIT", result.type);
        assertEquals(new BigDecimal("2500.00"), result.amount);
        assertEquals("SUCCESS", result.status);
        assertTrue(result.referenceId.startsWith("TXN-"));
    }

    @Test
    void should_processCredit_when_amountAtDailyLimit() {
        TransactionResult result = processor.processCredit(VALID_ACCOUNT_12,
                new BigDecimal("50000.00"), VALID_USER);
        assertNotNull(result);
        assertEquals("CREDIT", result.type);
    }
}
