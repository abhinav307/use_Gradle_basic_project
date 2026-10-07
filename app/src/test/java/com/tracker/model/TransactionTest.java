package com.tracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Transaction model class.
 */
class TransactionTest {

    @Test
    void testCostBasisCalculation() {
        Transaction t = new Transaction("AAPL", AssetType.STOCK, 10, 150.00, LocalDate.now());
        assertEquals(1500.00, t.getCostBasis(), 0.01);
    }

    @Test
    void testCostBasisWithFractionalQuantity() {
        Transaction t = new Transaction("bitcoin", AssetType.CRYPTO, 0.5, 42000.00, LocalDate.now());
        assertEquals(21000.00, t.getCostBasis(), 0.01);
    }

    @Test
    void testGettersAndSetters() {
        Transaction t = new Transaction();
        t.setId(1);
        t.setSymbol("TSLA");
        t.setAssetType(AssetType.STOCK);
        t.setQuantity(5);
        t.setPurchasePrice(200.00);
        t.setPurchaseDate(LocalDate.of(2024, 6, 15));

        assertEquals(1, t.getId());
        assertEquals("TSLA", t.getSymbol());
        assertEquals(AssetType.STOCK, t.getAssetType());
        assertEquals(5, t.getQuantity());
        assertEquals(200.00, t.getPurchasePrice());
        assertEquals(LocalDate.of(2024, 6, 15), t.getPurchaseDate());
    }

    @Test
    void testToString() {
        Transaction t = new Transaction("ETH", AssetType.CRYPTO, 2.5, 3000.00, LocalDate.of(2024, 1, 1));
        String str = t.toString();
        assertTrue(str.contains("ETH"));
        assertTrue(str.contains("CRYPTO"));
    }
}
