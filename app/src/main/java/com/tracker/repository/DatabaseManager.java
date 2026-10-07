package com.tracker.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the embedded H2 database connection and schema initialization.
 * The database file is stored locally so data persists between application runs.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./portfolio_data";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private Connection connection;

    /**
     * Initializes the database connection and creates tables if they don't exist.
     * @throws SQLException if a database access error occurs
     */
    public void initialize() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        createTables();
    }

    /**
     * Creates the transactions and alerts tables if they do not already exist.
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    symbol VARCHAR(20) NOT NULL,
                    asset_type VARCHAR(10) NOT NULL,
                    quantity DOUBLE NOT NULL,
                    purchase_price DOUBLE NOT NULL,
                    purchase_date DATE NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS alerts (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    symbol VARCHAR(20) NOT NULL,
                    threshold_price DOUBLE NOT NULL,
                    is_active BOOLEAN DEFAULT TRUE
                )
            """);
        }
    }

    /**
     * Returns the active database connection.
     * @return the JDBC Connection object
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the database connection gracefully.
     */
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}
