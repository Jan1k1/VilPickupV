package org.jan1k.vilpickup.database;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class VillagerDataService {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;

    public VillagerDataService(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void saveVillagerData(LivingEntity entity, String nbtData, String equipmentData) {
        if (!databaseManager.isEnabled()) {
            return;
        }

        String insertSQL = """
            INSERT INTO villager_data (world, x, y, z, yaw, pitch, entity_type, nbt_data, 
                                     profession, villager_level, equipment_data, custom_name) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {

            Location location = entity.getLocation();
            statement.setString(1, location.getWorld().getName());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());
            statement.setFloat(5, location.getYaw());
            statement.setFloat(6, location.getPitch());
            statement.setString(7, entity.getType().name());
            statement.setString(8, nbtData);

            if (entity instanceof Villager villager) {
                statement.setString(9, villager.getProfession().name());
                statement.setInt(10, villager.getVillagerLevel());
            } else {
                statement.setString(9, null);
                statement.setInt(10, 0);
            }

            statement.setString(11, equipmentData);
            statement.setString(12, entity.getCustomName());

            statement.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save villager data to database", e);
        }
    }

    public List<VillagerData> getVillagersInRadius(Location center, double radius) {
        if (!databaseManager.isEnabled()) {
            return new ArrayList<>();
        }

        String selectSQL = """
            SELECT * FROM villager_data 
            WHERE world = ? 
            AND SQRT(POW(x - ?, 2) + POW(y - ?, 2) + POW(z - ?, 2)) <= ?
            ORDER BY created_at DESC
            """;

        List<VillagerData> villagers = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSQL)) {

            statement.setString(1, center.getWorld().getName());
            statement.setDouble(2, center.getX());
            statement.setDouble(3, center.getY());
            statement.setDouble(4, center.getZ());
            statement.setDouble(5, radius);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                villagers.add(createVillagerDataFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to retrieve villagers from database", e);
        }

        return villagers;
    }

    public List<VillagerData> getVillagersByWorld(String worldName) {
        if (!databaseManager.isEnabled()) {
            return new ArrayList<>();
        }

        String selectSQL = "SELECT * FROM villager_data WHERE world = ? ORDER BY created_at DESC";
        List<VillagerData> villagers = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSQL)) {

            statement.setString(1, worldName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                villagers.add(createVillagerDataFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to retrieve villagers by world from database", e);
        }

        return villagers;
    }

    public void deleteVillagerData(int id) {
        if (!databaseManager.isEnabled()) {
            return;
        }

        String deleteSQL = "DELETE FROM villager_data WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteSQL)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete villager data from database", e);
        }
    }

    public void cleanupOldData(int daysOld) {
        if (!databaseManager.isEnabled()) {
            return;
        }

        String deleteSQL = "DELETE FROM villager_data WHERE created_at < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteSQL)) {

            statement.setInt(1, daysOld);
            int deletedRows = statement.executeUpdate();
            
            if (deletedRows > 0) {
                plugin.getLogger().info(String.format("Cleaned up %d old villager records", deletedRows));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to cleanup old villager data", e);
        }
    }

    public int getTotalVillagerCount() {
        if (!databaseManager.isEnabled()) {
            return 0;
        }

        String countSQL = "SELECT COUNT(*) FROM villager_data";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(countSQL);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get total villager count", e);
        }

        return 0;
    }

    private VillagerData createVillagerDataFromResultSet(ResultSet resultSet) throws SQLException {
        return new VillagerData(
                resultSet.getInt("id"),
                resultSet.getString("world"),
                resultSet.getDouble("x"),
                resultSet.getDouble("y"),
                resultSet.getDouble("z"),
                resultSet.getFloat("yaw"),
                resultSet.getFloat("pitch"),
                EntityType.valueOf(resultSet.getString("entity_type")),
                resultSet.getString("nbt_data"),
                resultSet.getString("profession"),
                resultSet.getInt("villager_level"),
                resultSet.getString("equipment_data"),
                resultSet.getString("custom_name"),
                resultSet.getTimestamp("created_at"),
                resultSet.getTimestamp("updated_at")
        );
    }

    public static class VillagerData {
        private final int id;
        private final String world;
        private final double x, y, z;
        private final float yaw, pitch;
        private final EntityType entityType;
        private final String nbtData;
        private final String profession;
        private final int villagerLevel;
        private final String equipmentData;
        private final String customName;
        private final Timestamp createdAt;
        private final Timestamp updatedAt;

        public VillagerData(int id, String world, double x, double y, double z, float yaw, float pitch,
                           EntityType entityType, String nbtData, String profession, int villagerLevel,
                           String equipmentData, String customName, Timestamp createdAt, Timestamp updatedAt) {
            this.id = id;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.entityType = entityType;
            this.nbtData = nbtData;
            this.profession = profession;
            this.villagerLevel = villagerLevel;
            this.equipmentData = equipmentData;
            this.customName = customName;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public int getId() { return id; }
        public String getWorld() { return world; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public float getYaw() { return yaw; }
        public float getPitch() { return pitch; }
        public EntityType getEntityType() { return entityType; }
        public String getNbtData() { return nbtData; }
        public String getProfession() { return profession; }
        public int getVillagerLevel() { return villagerLevel; }
        public String getEquipmentData() { return equipmentData; }
        public String getCustomName() { return customName; }
        public Timestamp getCreatedAt() { return createdAt; }
        public Timestamp getUpdatedAt() { return updatedAt; }
    }
}
