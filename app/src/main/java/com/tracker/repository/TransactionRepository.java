package com.tracker.repository;

import com.tracker.model.AssetType;
import com.tracker.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for CRUD operations on Transaction records.
 * Interacts directly with the embedded H2 database via JDBC.
 */
public class TransactionRepository {

    private final Connection connection;

    public TransactionRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Saves a new transaction to the database.
     * @param transaction the transaction to persist
     * @throws SQLException if a database error occurs
     */
    public void save(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (symbol, asset_type, quantity, purchase_price, purchase_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, transaction.getSymbol().toUpperCase());
            pstmt.setString(2, transaction.getAssetType().name());
            pstmt.setDouble(3, transaction.getQuantity());
            pstmt.setDouble(4, transaction.getPurchasePrice());
            pstmt.setDate(5, Date.valueOf(transaction.getPurchaseDate()));
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Retrieves all transactions from the database.
     * @return a list of all stored transactions
     * @throws SQLException if a database error occurs
     */
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY purchase_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setSymbol(rs.getString("symbol"));
                t.setAssetType(AssetType.valueOf(rs.getString("asset_type")));
                t.setQuantity(rs.getDouble("quantity"));
                t.setPurchasePrice(rs.getDouble("purchase_price"));
                t.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
                transactions.add(t);
            }
        }
        return transactions;
    }

    /**
     * Finds all transactions for a specific asset symbol.
     * @param symbol the ticker symbol to search for
     * @return list of matching transactions
     * @throws SQLException if a database error occurs
     */
    public List<Transaction> findBySymbol(String symbol) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE symbol = ? ORDER BY purchase_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, symbol.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setId(rs.getInt("id"));
                    t.setSymbol(rs.getString("symbol"));
                    t.setAssetType(AssetType.valueOf(rs.getString("asset_type")));
                    t.setQuantity(rs.getDouble("quantity"));
                    t.setPurchasePrice(rs.getDouble("purchase_price"));
                    t.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
                    transactions.add(t);
                }
            }
        }
        return transactions;
    }

    /**
     * Deletes a transaction by its ID.
     * @param id the transaction ID to delete
     * @throws SQLException if a database error occurs
     */
    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
