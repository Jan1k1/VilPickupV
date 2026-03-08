package org.jan1k.vilpickup.database;

import org.jan1k.vilpickup.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private HikariDataSource dataSource;
    private boolean enabled = false;

    public DatabaseManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void initialize() {
        if (!configManager.isDatabaseEnabled()) {
            plugin.getLogger().info("Database is disabled in config");
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:mysql:
                    configManager.getDatabaseHost(),
                    configManager.getDatabasePort(),
                    configManager.getDatabaseName()));
            config.setUsername(configManager.getDatabaseUsername());
            config.setPassword(configManager.getDatabasePassword());
            config.setConnectionTimeout(configManager.getDatabaseConnectionTimeout());
            config.setIdleTimeout(configManager.getDatabaseIdleTimeout());
            config.setMaxLifetime(configManager.getDatabaseMaxLifetime());
            config.setMaximumPoolSize(configManager.getDatabaseMaxPoolSize());
            config.setMinimumIdle(configManager.getDatabaseMinIdle());
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            dataSource = new HikariDataSource(config);
            
            createTables();
            enabled = true;
            plugin.getLogger().info("MySQL database connection established");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL database", e);
            enabled = false;
        }
    }

    private void createTables() throws SQLException {
        String createVillagerTable = """
            CREATE TABLE IF NOT EXISTS villager_data (
                id INT AUTO_INCREMENT PRIMARY KEY,
                world VARCHAR(255) NOT NULL,
                x DOUBLE NOT NULL,
                y DOUBLE NOT NULL,
                z DOUBLE NOT NULL,
                yaw FLOAT NOT NULL,
                pitch FLOAT NOT NULL,
                entity_type VARCHAR(50) NOT NULL,
                nbt_data TEXT NOT NULL,
                profession VARCHAR(50),
                villager_level INT,
                equipment_data TEXT,
                custom_name VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_location (world, x, y, z),
                INDEX idx_type (entity_type),
                INDEX idx_created (created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(createVillagerTable)) {
            statement.execute();
        }
    }

    public Connection getConnection() throws SQLException {
        if (!enabled || dataSource == null) {
            throw new SQLException("Database is not enabled or initialized");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
