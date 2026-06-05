package org.jan1k.vilpickup.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import com.destroystokyo.paper.profile.PlayerProfile;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class VillagerHeads {

    public static final String DEFAULT_TEXTURE = "http://textures.minecraft.net/texture/f9a8dbf35295c8088198f8fc85785f0965377033ea7dc07963b61f85584555f1";
    public static final String BABY_TEXTURE = "http://textures.minecraft.net/texture/414f664c3c3a96863777d85e7869ec1f99c15e865328575a6cc331da9f62";
    public static final String ZOMBIE_TEXTURE = "http://textures.minecraft.net/texture/f12e11e03a118bdd5f899d424b95d03bb560b37cd4a3d4f19b0cc8a63584860";
    public static final String BABY_ZOMBIE_TEXTURE = "http://textures.minecraft.net/texture/719f96b991da3522ce2d733189a8731f8f3b06606f7596853744641951f2";

    public static final Map<Villager.Profession, String> TEXTURE_MAP = Map.ofEntries(
            Map.entry(Villager.Profession.ARMORER, "http://textures.minecraft.net/texture/26df165f1715456c32115662df46f90352ef2ebb4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.BUTCHER, "http://textures.minecraft.net/texture/26ed090413f185bdd5f899d424b95d03bb560b37cd4a3d4f19b0cc8a63584860"),
            Map.entry(Villager.Profession.CARTOGRAPHER, "http://textures.minecraft.net/texture/6e64ecd35359b863777d85e7869ec1f99c15e865328575a6cc331da9f62"),
            Map.entry(Villager.Profession.CLERIC, "http://textures.minecraft.net/texture/f9a8dbf35295c8088198f8fc85785f0965377033ea7dc07963b61f85584555f1"),
            Map.entry(Villager.Profession.FARMER, "http://textures.minecraft.net/texture/26f043657b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.FISHERMAN, "http://textures.minecraft.net/texture/2af66ac7b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.FLETCHER, "http://textures.minecraft.net/texture/26f043657b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.LEATHERWORKER, "http://textures.minecraft.net/texture/26f043657b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.LIBRARIAN, "http://textures.minecraft.net/texture/f9a8dbf35295c8088198f8fc85785f0965377033ea7dc07963b61f85584555f1"),
            Map.entry(Villager.Profession.MASON, "http://textures.minecraft.net/texture/26f043657b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.NITWIT, "http://textures.minecraft.net/texture/26f043657b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.NONE, "http://textures.minecraft.net/texture/f9a8dbf35295c8088198f8fc85785f0965377033ea7dc07963b61f85584555f1"),
            Map.entry(Villager.Profession.SHEPHERD, "http://textures.minecraft.net/texture/26f043657b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.TOOLSMITH, "http://textures.minecraft.net/texture/26f043657b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380"),
            Map.entry(Villager.Profession.WEAPONSMITH, "http://textures.minecraft.net/texture/26f043657b545d9e5b61e27150c9509b552eff4860db35c91f6d32832598380")
    );

    public static ItemStack createVillagerHead(LivingEntity entity, String displayName) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        setEntityTexture(item, entity);
        
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null && displayName != null) {
            meta.displayName(Utils.legacyComponent(displayName));
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public static void setEntityTexture(ItemStack item, LivingEntity entity) {
        try {
            URL texture = getTexture(entity);
            setEntityTexture(item, texture);
        } catch (Exception exception) {
            JavaPlugin.getProvidingPlugin(VillagerHeads.class).getLogger().warning("Failed to set villager texture: " + exception.getMessage());
        }
    }

    private static void setEntityTexture(ItemStack item, URL texture) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return;
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getTextures().setSkin(texture);
        meta.setPlayerProfile(profile);
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
