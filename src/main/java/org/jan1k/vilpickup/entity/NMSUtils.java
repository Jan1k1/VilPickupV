package org.jan1k.vilpickup.entity;

import org.bukkit.Bukkit;

public class NMSUtils {
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackageName();

    public static String cbClass(String clazz) {
        return CRAFTBUKKIT_PACKAGE + "." + clazz;
    }
}
