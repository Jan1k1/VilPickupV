package org.jan1k.vilpickup.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Method;

public class NMSSaver implements EntitySaver {
    private static final Class<?> CRAFT_ENTITY_CLASS;
    private static final Class<?> NMS_ENTITY_CLASS;
    private static final Class<?> COMPOUND_TAG_CLASS;
    private static final Class<?> TAG_PARSER_CLASS;
    private static final Method GET_HANDLE_METHOD;
    private static final Method SAVE_METHOD;
    private static final Method LOAD_METHOD;
    private static final Method TO_STRING_METHOD;
    private static final Method PARSE_TAG_METHOD;
    private static final Method PUT_UUID_METHOD;
    private static final Object COMPOUND_TAG_INSTANCE;

    static {
        try {
            CRAFT_ENTITY_CLASS = Class.forName(NMSUtils.cbClass("entity.CraftEntity"));
            GET_HANDLE_METHOD = CRAFT_ENTITY_CLASS.getMethod("getHandle");
            String nmsPackage = findNMSPackage();
            NMS_ENTITY_CLASS = Class.forName(nmsPackage + ".Entity");
            
            String nbtPackage;
            if (nmsPackage.equals("net.minecraft.world.entity")) {
                nbtPackage = "net.minecraft.nbt";
            } else {
                nbtPackage = nmsPackage.replace(".Entity", ".NBTTagCompound").replace("Entity", "");
                if (nbtPackage.endsWith(".")) {
                    nbtPackage = nbtPackage.substring(0, nbtPackage.length() - 1);
                }
            }
            
            Class<?> tempCompoundTag;
            Class<?> tempTagParser;
            
            try {
                tempCompoundTag = Class.forName(nbtPackage + ".CompoundTag");
                tempTagParser = Class.forName(nbtPackage + ".TagParser");
            } catch (ClassNotFoundException e) {
                try {
                    tempCompoundTag = Class.forName(nbtPackage + ".NBTTagCompound");
                    tempTagParser = Class.forName(nbtPackage + ".MojangsonParser");
                } catch (ClassNotFoundException e2) {
                    tempCompoundTag = Class.forName("net.minecraft.nbt.CompoundTag");
                    tempTagParser = Class.forName("net.minecraft.nbt.TagParser");
                }
            }
            
            COMPOUND_TAG_CLASS = tempCompoundTag;
            TAG_PARSER_CLASS = tempTagParser;
            
            String[] saveMethodNames = {"save", "saveAsPassenger", "addAdditionalSaveData", "b", "f", "saveWithoutId"};
            String[] loadMethodNames = {"load", "readAdditionalSaveData", "a", "g", "readFromNBT"};
            String[] toStringMethodNames = {"toString", "asString", "getAsString"};
            String[] parseTagMethodNames = {"parseTag", "parse", "a"};
            String[] putUUIDMethodNames = {"putUUID", "setUUID", "a", "putId"};
            
            Method tempSaveMethod = findMethod(NMS_ENTITY_CLASS, saveMethodNames, COMPOUND_TAG_CLASS);
            Method tempLoadMethod = findMethod(NMS_ENTITY_CLASS, loadMethodNames, COMPOUND_TAG_CLASS);
            Method tempToStringMethod = findMethod(COMPOUND_TAG_CLASS, toStringMethodNames);
            Method tempParseTagMethod = findStaticMethod(TAG_PARSER_CLASS, parseTagMethodNames, String.class);
            Method tempPutUUIDMethod = findMethod(COMPOUND_TAG_CLASS, putUUIDMethodNames, String.class, java.util.UUID.class);
            
            SAVE_METHOD = tempSaveMethod;
            LOAD_METHOD = tempLoadMethod;
            TO_STRING_METHOD = tempToStringMethod;
            PARSE_TAG_METHOD = tempParseTagMethod;
            PUT_UUID_METHOD = tempPutUUIDMethod;
            
            COMPOUND_TAG_INSTANCE = COMPOUND_TAG_CLASS.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("NMS initialization failed", e);
        }
    }
    
    private static String findNMSPackage() throws ClassNotFoundException {
        String[] patterns = {
            "net.minecraft.world.entity",
            "net.minecraft.server.level",
            "net.minecraft.server.v1_19_R3",
            "net.minecraft.server.v1_20_R1", 
            "net.minecraft.server.v1_20_R2",
            "net.minecraft.server.v1_20_R3"
        };
        
        for (String pattern : patterns) {
            try {
                if (pattern.equals("net.minecraft.world.entity")) {
                    Class.forName(pattern + ".Entity");
                    return pattern;
                } else if (pattern.equals("net.minecraft.server.level")) {
                    Class.forName(pattern + ".ServerLevel");
                    return "net.minecraft.world.entity";
                } else {
                    Class.forName(pattern + ".Entity");
                    return pattern;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException("Could not find NMS Entity class");
    }
    
    private static Method findMethod(Class<?> clazz, String[] methodNames, Class<?>... paramTypes) throws NoSuchMethodException {
        for (String methodName : methodNames) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("No suitable method found in " + clazz.getName());
    }
    
    private static Method findStaticMethod(Class<?> clazz, String[] methodNames, Class<?>... paramTypes) throws NoSuchMethodException {
        for (String methodName : methodNames) {
            try {
                Method method = clazz.getMethod(methodName, paramTypes);
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        for (String methodName : methodNames) {
            try {
                return clazz.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("No suitable static method found in " + clazz.getName());
    }

    @Override
    public String writeToString(Entity entity) throws IllegalArgumentException {
        try {
            Object nmsEntity = getNmsEntity(entity);
            Object nbt = COMPOUND_TAG_CLASS.getConstructor().newInstance();
            SAVE_METHOD.invoke(nmsEntity, nbt);
            return (String) TO_STRING_METHOD.invoke(nbt);
        } catch (Exception e) {
            throw new IllegalArgumentException("NBT write failed: " + entity, e);
        }
    }

    @Override
    public Entity readAndSpawnAt(String string, EntityType type, Location location) throws IllegalArgumentException {
        try {
            World world = location.getWorld();
            if (world == null) {
                throw new IllegalArgumentException("World cannot be null");
            }
            Entity entity = world.spawnEntity(location, type);
            Object nbt = PARSE_TAG_METHOD.invoke(null, string);
            PUT_UUID_METHOD.invoke(nbt, "UUID", entity.getUniqueId());
            Object nmsEntity = getNmsEntity(entity);
            LOAD_METHOD.invoke(nmsEntity, nbt);
            entity.teleport(location);
            return entity;
        } catch (Exception e) {
            throw new IllegalArgumentException("NBT read failed: " + string, e);
        }
    }

    private static Object getNmsEntity(Entity entity) throws Exception {
        Object nmsEntity = CRAFT_ENTITY_CLASS.cast(entity);
        return GET_HANDLE_METHOD.invoke(nmsEntity);
    }
}
