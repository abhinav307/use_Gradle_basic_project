package com.tracker.model;

import java.time.LocalDate;

/**
 * Represents a single buy/sell transaction for a financial asset.
 * Tracks the asset symbol, quantity, purchase price, date, and type.
 */
public class Transaction {
    private int id;
    private String symbol;
    private AssetType assetType;
    private double quantity;
    private double purchasePrice;
    private LocalDate purchaseDate;

    public Transaction() {}

    public Transaction(String symbol, AssetType assetType, double quantity, double purchasePrice, LocalDate purchaseDate) {
        this.symbol = symbol;
        this.assetType = assetType;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    /**
     * Calculates the total cost basis for this transaction.
     * @return quantity * purchasePrice
     */
    public double getCostBasis() {
        return quantity * purchasePrice;
    }

    @Override
    public String toString() {
        return String.format("Transaction{symbol='%s', type=%s, qty=%.4f, price=%.2f, date=%s}",
                symbol, assetType, quantity, purchasePrice, purchaseDate);
    }
}
