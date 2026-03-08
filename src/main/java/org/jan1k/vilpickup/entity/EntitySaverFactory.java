package org.jan1k.vilpickup.entity;

public class EntitySaverFactory {
    private static boolean isEntitySnapshotSupported;
    private static boolean isNMSSupported;

    static {
        try {
            Class.forName("org.bukkit.entity.EntitySnapshot");
            Class.forName("org.bukkit.entity.EntityFactory");
            isEntitySnapshotSupported = true;
        } catch (ClassNotFoundException exception) {
            isEntitySnapshotSupported = false;
        }
        
        try {
            new NMSSaver();
            isNMSSupported = true;
        } catch (RuntimeException exception) {
            isNMSSupported = false;
        }
    }

    public static EntitySaver create() {
        if (isEntitySnapshotSupported) {
            return new SnapshotSaver();
        }
        if (isNMSSupported) {
            return new NMSSaver();
        }
        throw new RuntimeException("No compatible EntitySaver found - requires EntitySnapshot or NMS access");
    }
    
    public static boolean isSupported() {
        return isEntitySnapshotSupported || isNMSSupported;
    }
}
