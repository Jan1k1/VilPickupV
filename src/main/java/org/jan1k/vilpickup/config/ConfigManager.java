package org.jan1k.vilpickup.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    public void loadConfigs() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public boolean requireShift() {
        return config.getBoolean("settings.require-shift", true);
    }
    
    public boolean requireSneak() {
        return config.getBoolean("settings.require-sneak", false);
    }
    
    public boolean requirePermission() {
        return config.getBoolean("settings.require-permission", false);
    }
    
    public String getPermissionNode() {
        return config.getString("settings.permission-node", "vilpickup.use");
    }
    
    public List<String> getAllowedEntities() {
        return config.getStringList("settings.allowed-entities");
    }
    
    public boolean isPickupSoundEnabled() {
        return config.getBoolean("settings.pickup-sound", true);
    }
    
    public String getSoundType() {
        return config.getString("settings.sound-type", "ENTITY_ITEM_PICKUP");
    }
    
    public float getSoundVolume() {
        return (float) config.getDouble("settings.sound-volume", 1.0);
    }
    
    public float getSoundPitch() {
        return (float) config.getDouble("settings.sound-pitch", 1.0);
    }
    
    public double getMaxPickupDistance() {
        return config.getDouble("restrictions.max-pickup-distance", 5.0);
    }
    
    public int getCooldownSeconds() {
        return config.getInt("restrictions.cooldown-seconds", 0);
    }
    
    public boolean preventPickupInCombat() {
        return config.getBoolean("restrictions.prevent-pickup-in-combat", false);
    }
    
    public int getCombatTimeSeconds() {
        return config.getInt("restrictions.combat-time-seconds", 10);
    }
    
    public String getItemMaterial() {
        return config.getString("item.material", "PLAYER_HEAD");
    }
    
    public String getItemName() {
        return config.getString("item.name", "&7[Villager] &f{name}");
    }
    
    public List<String> getItemLore() {
        return config.getStringList("item.lore");
    }
    
    public boolean isItemGlow() {
        return config.getBoolean("item.glow", true);
    }
    
    public boolean requireShiftPlace() {
        return config.getBoolean("placement.require-shift-place", false);
    }
    
    public boolean preventPlaceInClaimed() {
        return config.getBoolean("placement.prevent-place-in-claimed", false);
    }
    
    public boolean isPlaceSoundEnabled() {
        return config.getBoolean("placement.place-sound", true);
    }
    
    public String getPlaceSoundType() {
        return config.getString("placement.place-sound-type", "ENTITY_VILLAGER_AMBIENT");
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }
    
    public boolean logPickups() {
        return config.getBoolean("debug.log-pickups", false);
    }
    
    public boolean logPlacements() {
        return config.getBoolean("debug.log-placements", false);
    }
    
    public String getMessage(String path) {
        String message = messages.getString("messages." + path, "");
        String prefix = messages.getString("prefix", "");
        return colorize(prefix + message);
    }
    
    public String getMessageNoPrefix(String path) {
        return colorize(messages.getString("messages." + path, ""));
    }
    
    public boolean isDatabaseEnabled() {
        return config.getBoolean("database.enabled", false);
    }
    
    public String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }
    
    public int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }
    
    public String getDatabaseName() {
        return config.getString("database.database", "vilpickup");
    }
    
    public String getDatabaseUsername() {
        return config.getString("database.username", "root");
    }
    
    public String getDatabasePassword() {
        return config.getString("database.password", "password");
    }
    
    public long getDatabaseConnectionTimeout() {
        return config.getLong("database.connection-timeout", 30000);
    }
    
    public long getDatabaseIdleTimeout() {
        return config.getLong("database.idle-timeout", 600000);
    }
    
    public long getDatabaseMaxLifetime() {
        return config.getLong("database.max-lifetime", 1800000);
    }
    
    public int getDatabaseMaxPoolSize() {
        return config.getInt("database.maximum-pool-size", 10);
    }
    
    public int getDatabaseMinIdle() {
        return config.getInt("database.minimum-idle", 5);
    }
    
    private String colorize(String text) {
        return text.replace("&", "§");
    }
}
