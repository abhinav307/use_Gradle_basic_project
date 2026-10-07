package com.tracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a user's complete financial portfolio.
 * Aggregates all transactions and provides summary views.
 */
public class Portfolio {
    private String name;
    private List<Transaction> transactions;

    public Portfolio(String name) {
        this.name = name;
        this.transactions = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Groups all transactions by their asset symbol.
     * @return a map of symbol to list of transactions
     */
    public Map<String, List<Transaction>> getTransactionsBySymbol() {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getSymbol));
    }

    /**
     * Returns a list of unique asset symbols in this portfolio.
     * @return list of distinct symbols
     */
    public List<String> getUniqueSymbols() {
        return transactions.stream()
                .map(Transaction::getSymbol)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total cost basis across all transactions.
     * @return sum of (quantity * purchasePrice) for all transactions
     */
    public double getTotalCostBasis() {
        return transactions.stream()
                .mapToDouble(Transaction::getCostBasis)
                .sum();
    }
}
