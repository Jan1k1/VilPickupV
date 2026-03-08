package org.jan1k.vilpickup.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class VillagerHeads {

    public static final String DEFAULT_TEXTURE = "http:
    public static final String BABY_TEXTURE = "http:
    public static final String ZOMBIE_TEXTURE = "http:
    public static final String BABY_ZOMBIE_TEXTURE = "http:

    public static final Map<Villager.Profession, String> TEXTURE_MAP = Map.ofEntries(
            Map.entry(Villager.Profession.FISHERMAN, "http:
            Map.entry(Villager.Profession.ARMORER, "http:
            Map.entry(Villager.Profession.BUTCHER, "http:
            Map.entry(Villager.Profession.CARTOGRAPHER, "http:
            Map.entry(Villager.Profession.CLERIC, "http:
            Map.entry(Villager.Profession.FARMER, "http:
            Map.entry(Villager.Profession.FLETCHER, "http:
            Map.entry(Villager.Profession.LEATHERWORKER, "http:
            Map.entry(Villager.Profession.LIBRARIAN, "http:
            Map.entry(Villager.Profession.MASON, "http:
            Map.entry(Villager.Profession.NITWIT, "http:
            Map.entry(Villager.Profession.NONE, "http:
            Map.entry(Villager.Profession.SHEPHERD, "http:
            Map.entry(Villager.Profession.TOOLSMITH, "http:
            Map.entry(Villager.Profession.WEAPONSMITH, "http:
    );

    public static ItemStack createVillagerHead(LivingEntity entity, String displayName) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        setEntityTexture(item, entity);
        
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null && displayName != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public static void setEntityTexture(ItemStack item, LivingEntity entity) {
        try {
            URL texture = getTexture(entity);
            setEntityTexture(item, texture);
        } catch (Exception exception) {
            System.err.println("Failed to set villager texture: " + exception.getMessage());
        }
    }

    private static void setEntityTexture(ItemStack item, URL texture) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return;
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        profile.getTextures().setSkin(texture);
        meta.setOwnerProfile(profile);
        item.setItemMeta(meta);
    }

    private static URL getTexture(LivingEntity entity) throws Exception {
        boolean isZombie = entity instanceof ZombieVillager;
        if (!(entity instanceof Villager) && !isZombie) {
            throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass().getName());
        }
        
        Villager.Profession profession = Utils.getVillagerProfession(entity);
        boolean isAdult = ((Ageable) entity).isAdult();
        
        if (isZombie && !isAdult) {
            return URI.create(BABY_ZOMBIE_TEXTURE).toURL();
        }
        if (!isAdult) {
            return URI.create(BABY_TEXTURE).toURL();
        }
        if (isZombie) {
            return URI.create(ZOMBIE_TEXTURE).toURL();
        }
        return URI.create(TEXTURE_MAP.getOrDefault(profession, DEFAULT_TEXTURE)).toURL();
    }
}
