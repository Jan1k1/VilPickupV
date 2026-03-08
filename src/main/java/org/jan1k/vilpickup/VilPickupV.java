package org.jan1k.vilpickup;

import org.jan1k.vilpickup.config.ConfigManager;
import org.jan1k.vilpickup.database.DatabaseManager;
import org.jan1k.vilpickup.database.VillagerDataService;
import org.jan1k.vilpickup.entity.EntitySaver;
import org.jan1k.vilpickup.entity.EntitySaverFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class VilPickupV extends JavaPlugin {

	private ConfigManager configManager;
	private DatabaseManager databaseManager;
	private VillagerDataService villagerDataService;
	private PickupManager pickupManager;
	private InteractListener interactListener;

	@Override
	public void onEnable() {
		getLogger().info("VilPickupV Starting Up");
		
		if (!EntitySaverFactory.isSupported()) {
			getLogger().severe("Server doesn't support villager data saving");
			getLogger().severe("Plugin disabled");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		configManager = new ConfigManager(this);
		
		databaseManager = new DatabaseManager(this, configManager);
		databaseManager.initialize();
		
		villagerDataService = new VillagerDataService(this, databaseManager);
		
		EntitySaver entitySaver = EntitySaverFactory.create();
		String saverType = entitySaver.getClass().getSimpleName();
		if (saverType.equals("SnapshotSaver")) {
			getLogger().info("Using EntitySnapshot API");
		} else {
			getLogger().info("Using NMS reflection");
		}
		
		pickupManager = new PickupManager(this, entitySaver, configManager, villagerDataService);
		interactListener = new InteractListener(this, pickupManager, configManager);
		
		getLogger().info("VilPickupV Ready - Shift+right-click villagers");
	}

	@Override
	public void onDisable() {
		getLogger().info("VilPickupV shutting down...");
		
		if (databaseManager != null) {
			databaseManager.close();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("vilpickup")) {
			if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("vilpickup.admin")) {
					sender.sendMessage(configManager.getMessage("general.no-permission"));
					return true;
				}
				configManager.reloadConfigs();
				sender.sendMessage(configManager.getMessage("general.config-reloaded"));
				return true;
			}
		}
		return false;
	}
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
}
