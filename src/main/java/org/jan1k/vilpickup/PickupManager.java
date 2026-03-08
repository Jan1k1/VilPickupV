package org.jan1k.vilpickup;

import org.jan1k.vilpickup.config.ConfigManager;
import org.jan1k.vilpickup.database.VillagerDataService;
import org.jan1k.vilpickup.entity.EntitySaver;
import org.jan1k.vilpickup.util.Utils;
import org.jan1k.vilpickup.util.VillagerHeads;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.entity.Ageable;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PickupManager implements Listener {
    
    private enum VillagerType {
        VILLAGER,
        ZOMBIE;

        EntityType toEntityType() {
            return switch (this) {
                case VILLAGER -> EntityType.VILLAGER;
                case ZOMBIE -> EntityType.ZOMBIE_VILLAGER;
            };
        }
    }

    private final NamespacedKey VILLAGER_KEY;
    private final NamespacedKey TYPE_KEY;
    private final NamespacedKey NBT_KEY;
    private final NamespacedKey PROFESSION_KEY;
    private final NamespacedKey LEVEL_KEY;
    private final NamespacedKey EQUIPMENT_KEY;
    private final EntitySaver entitySaver;
    private final ConfigManager configManager;
    private final VillagerDataService villagerDataService;

    public PickupManager(JavaPlugin plugin, EntitySaver entitySaver, ConfigManager configManager, VillagerDataService villagerDataService) {
        this.entitySaver = entitySaver;
        this.configManager = configManager;
        this.villagerDataService = villagerDataService;
        
        this.VILLAGER_KEY = new NamespacedKey(plugin, "villager");
        this.TYPE_KEY = new NamespacedKey(plugin, "type");
        this.NBT_KEY = new NamespacedKey(plugin, "nbt");
        this.PROFESSION_KEY = new NamespacedKey(plugin, "profession");
        this.LEVEL_KEY = new NamespacedKey(plugin, "level");
        this.EQUIPMENT_KEY = new NamespacedKey(plugin, "equipment");
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemResult itemResult = getHeldVillagerItem(inventory);
        if (itemResult == null) return;
        event.setCancelled(true);
        
        Block block = event.getBlockPlaced();
        Location location = block.getLocation().add(.5, 0, .5);
        float yaw = player.getLocation().getYaw();
        location.setYaw((yaw + 360) % 360 - 180);
        
        try {
            ItemStack item = itemResult.item();
            spawnFromItemStack(item, location);
            itemResult.decrementAmount(inventory);
            World world = player.getWorld();
            
            if (configManager.isPlaceSoundEnabled()) {
                Sound sound = Sound.valueOf(configManager.getPlaceSoundType());
                world.playSound(location, sound, configManager.getSoundVolume(), configManager.getSoundPitch());
            }
            Block blockBelow = block.getRelative(BlockFace.DOWN);
            world.spawnParticle(Particle.BLOCK_CRACK, location, 30, blockBelow.getBlockData());
            
            player.sendMessage(configManager.getMessage("placement.success"));
        } catch (IllegalArgumentException exception) {
            player.sendMessage(configManager.getMessage("placement.placement-failed"));
        }
    }

    public ItemStack toItemStack(LivingEntity entity) throws IllegalArgumentException {
        String entityName = entity.getType().name();
        if (!configManager.getAllowedEntities().contains(entityName)) {
            throw new IllegalArgumentException("Entity type not allowed");
        }
        
        
        ItemStack item = createVillagerItem(entity);
        saveVillagerData(entity, item);
        
        if (villagerDataService != null) {
            String nbt = entitySaver.writeToString(entity);
            String equipmentData = entity instanceof ZombieVillager ? serializeEquipment(entity.getEquipment()) : null;
            villagerDataService.saveVillagerData(entity, nbt, equipmentData);
        }
        
        entity.remove();
        return item;
    }

    private ItemResult getHeldVillagerItem(PlayerInventory inventory) {
        ItemStack item = inventory.getItemInMainHand();
        if (isVillager(item)) {
            return new ItemResult(item, EquipmentSlot.HAND);
        }
        item = inventory.getItemInOffHand();
        if (isVillager(item)) {
            return new ItemResult(item, EquipmentSlot.OFF_HAND);
        }
        return null;
    }

    private void saveVillagerData(LivingEntity entity, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(VILLAGER_KEY, PersistentDataType.BOOLEAN, true);
        data.set(TYPE_KEY, PersistentDataType.STRING, (entity instanceof ZombieVillager)
                ? VillagerType.ZOMBIE.toString()
                : VillagerType.VILLAGER.toString());
        
        if (entity instanceof Villager villager) {
            data.set(PROFESSION_KEY, PersistentDataType.STRING, villager.getProfession().name());
            data.set(LEVEL_KEY, PersistentDataType.INTEGER, villager.getVillagerLevel());
        }
        
        if (entity instanceof ZombieVillager zombieVillager) {
            String equipmentData = serializeEquipment(zombieVillager.getEquipment());
            data.set(EQUIPMENT_KEY, PersistentDataType.STRING, equipmentData);
        }
        
        String nbt = entitySaver.writeToString(entity);
        data.set(NBT_KEY, PersistentDataType.STRING, nbt);
        item.setItemMeta(meta);
    }

    public void sendPickupEffect(LivingEntity entity) {
        Location location = entity.getLocation().add(0, .25, 0);
        World world = entity.getWorld();
        world.spawnParticle(Particle.SWEEP_ATTACK, location, 1);
        if (configManager.isPickupSoundEnabled()) {
            Sound sound = Sound.valueOf(configManager.getSoundType());
            world.playSound(location, sound, configManager.getSoundVolume(), configManager.getSoundPitch());
        }
    }

    public LivingEntity spawnFromItemStack(ItemStack item, Location location) throws IllegalArgumentException {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) throw new IllegalArgumentException("ItemMeta is null");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!data.has(VILLAGER_KEY, PersistentDataType.BOOLEAN))
            throw new IllegalArgumentException("Item is not a villager");
        if (!data.has(TYPE_KEY, PersistentDataType.STRING))
            throw new IllegalArgumentException("Villager type is missing");
        VillagerType villagerType = VillagerType.valueOf(data.get(TYPE_KEY, PersistentDataType.STRING));
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("World is null");
        EntityType type = villagerType.toEntityType();
        String nbt = data.get(NBT_KEY, PersistentDataType.STRING);
        LivingEntity spawnedEntity = (LivingEntity) entitySaver.readAndSpawnAt(nbt, type, location);
        
        if (spawnedEntity instanceof Villager spawnedVillager && data.has(PROFESSION_KEY, PersistentDataType.STRING)) {
            String professionName = data.get(PROFESSION_KEY, PersistentDataType.STRING);
            Integer level = data.get(LEVEL_KEY, PersistentDataType.INTEGER);
            
            if (professionName != null) {
                try {
                    Villager.Profession profession = Villager.Profession.valueOf(professionName);
                    spawnedVillager.setProfession(profession);
                    if (level != null) {
                        spawnedVillager.setVillagerLevel(level);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        
        if (spawnedEntity instanceof ZombieVillager spawnedZombie && data.has(EQUIPMENT_KEY, PersistentDataType.STRING)) {
            String equipmentData = data.get(EQUIPMENT_KEY, PersistentDataType.STRING);
            if (equipmentData != null) {
                deserializeEquipment(spawnedZombie.getEquipment(), equipmentData);
            }
        }
        
        return spawnedEntity;
    }

    public boolean isVillager(ItemStack item) {
        Material itemMaterial = Material.valueOf(configManager.getItemMaterial());
        if (item.getType() != itemMaterial) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.has(VILLAGER_KEY, PersistentDataType.BOOLEAN);
    }

    private ItemStack createVillagerItem(LivingEntity entity) {
        String customName = entity.getCustomName();
        String profession = "Unknown";
        String level = "1";
        String villagerType = "Villager";
        Villager.Profession villagerProfession = Villager.Profession.NONE;
        
        boolean isAdult = ((Ageable) entity).isAdult();
        boolean isZombie = entity instanceof ZombieVillager;
        
        if (entity instanceof Villager villager) {
            villagerProfession = villager.getProfession();
            profession = Utils.titleCase(villagerProfession.name().replace("_", " "));
            level = String.valueOf(villager.getVillagerLevel());
            
            if (!isAdult) {
                villagerType = "Baby " + profession;
            } else if (villagerProfession == Villager.Profession.NONE) {
                villagerType = "Unemployed Villager";
            } else {
                villagerType = profession;
            }
        } else if (isZombie) {
            profession = "Zombie";
            if (!isAdult) {
                villagerType = "Baby Zombie Villager";
            } else {
                villagerType = "Zombie Villager";
            }
        }
        
        String name = customName != null ? customName : villagerType;
        
        String displayName = configManager.getItemName()
            .replace("{name}", name)
            .replace("{profession}", profession)
            .replace("{level}", level)
            .replace("&", "§");
        
        ItemStack item;
        if (configManager.getItemMaterial().equals("PLAYER_HEAD")) {
            item = VillagerHeads.createVillagerHead(entity, villagerType);
        } else {
            Material itemMaterial = Material.valueOf(configManager.getItemMaterial());
            item = new ItemStack(itemMaterial);
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(villagerType);
            
            java.util.List<String> lore = new java.util.ArrayList<>();
            for (String line : configManager.getItemLore()) {
                lore.add(line.replace("{name}", name)
                    .replace("{profession}", profession)
                    .replace("{level}", level)
                    .replace("&", "§"));
            }
            meta.setLore(lore);
            
            if (configManager.isItemGlow()) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            
            item.setItemMeta(meta);
        }
        return item;
    }

    private String serializeEquipment(EntityEquipment equipment) {
        StringBuilder sb = new StringBuilder();
        ItemStack[] items = {
            equipment.getItemInMainHand(),
            equipment.getItemInOffHand(), 
            equipment.getHelmet(),
            equipment.getChestplate(),
            equipment.getLeggings(),
            equipment.getBoots()
        };
        
        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append("|");
            ItemStack item = items[i];
            if (item != null && item.getType() != Material.AIR) {
                sb.append(item.getType().name()).append(":").append(item.getAmount());
            }
        }
        return sb.toString();
    }
    
    private void deserializeEquipment(EntityEquipment equipment, String data) {
        if (data == null || data.isEmpty()) return;
        String[] parts = data.split("\\|");
        try {
            for (int i = 0; i < Math.min(parts.length, 6); i++) {
                if (parts[i].isEmpty()) continue;
                String[] itemParts = parts[i].split(":");
                Material material = Material.valueOf(itemParts[0]);
                int amount = Integer.parseInt(itemParts[1]);
                ItemStack item = new ItemStack(material, amount);
                
                switch (i) {
                    case 0 -> equipment.setItemInMainHand(item);
                    case 1 -> equipment.setItemInOffHand(item);
                    case 2 -> equipment.setHelmet(item);
                    case 3 -> equipment.setChestplate(item);
                    case 4 -> equipment.setLeggings(item);
                    case 5 -> equipment.setBoots(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to deserialize equipment: " + e.getMessage());
        }
    }

    private record ItemResult(ItemStack item, EquipmentSlot slot) {
        void decrementAmount(PlayerInventory inventory) {
            ItemStack item = this.item;
            item.setAmount(item.getAmount() - 1);
            inventory.setItem(slot, item);
        }
    }
}
