package com.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private Connection connection;
    private final Properties dbProps;

    public DatabaseManager(String configFile) {
        dbProps = new Properties();
        try {
            dbProps.load(getClass().getClassLoader().getResourceAsStream(configFile));
        } catch (Exception e) {
            logger.error("Failed to load input properties", e);
        }
        try {
            // Configure your MySQL connection
            String url = dbProps.getProperty("db.url"); //, "";
            String user = dbProps.getProperty("db.user");  // "root";
            String password = dbProps.getProperty("db.password");  ;

            connection = DriverManager.getConnection(url, user, password);
            initializeDatabase();
            logger.info("Connected to MySQL database");
        } catch (SQLException e) {
            logger.error("Database connection failed", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS InstrumentDetails (" +
                    " ID VARCHAR(100) PRIMARY KEY, "
                    + "ISIN VARCHAR(100) NOT NULL, "
                    + "Unit_price DECIMAL(19,4) NOT NULL, "
                    + "Name VARCHAR(255), "
                    + "CONSTRAINT isin_unique UNIQUE (ISIN)"
                    + ")" );

            stmt.execute("CREATE TABLE IF NOT EXISTS PostionDetails(" + "CREATE TABLE IF NOT EXISTS instruments ("
                    + "ID VARCHAR(100) PRIMARY KEY,"
                    + "Instrument_id VARCHAR(100) NOT NULL, "
                    + "Quantity INT NOT NULL"
                    + ")");
        }
    }

    public void storeData(String tableName, Map<String, String> data) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (id, value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE value = VALUES(value)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                pstmt.setString(1, entry.getKey());
                pstmt.setString(2, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            logger.debug("Stored {} records in {}", data.size(), tableName);
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing connection", e);
        }
    }
}