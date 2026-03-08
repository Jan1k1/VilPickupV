package org.jan1k.vilpickup.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public interface EntitySaver {
    String writeToString(Entity entity) throws IllegalArgumentException;
    Entity readAndSpawnAt(String string, EntityType entityType, Location location) throws IllegalArgumentException;
}
