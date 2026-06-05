package org.jan1k.vilpickup.util;

public class Jan1kStyle {
    
    public static final String PREFIX = "\u00A78[\u00A76jan1k\u00A78] \u00A77";
    public static final String BRAND = "\u00A76jan1k\u00A7r";
    public static final String PLUGIN_NAME = "\u00A77VilPickup\u00A7eV\u00A7r";
    public static final String SUCCESS = "\u00A7a";
    public static final String ERROR = "\u00A7c";
    public static final String WARNING = "\u00A7e";
    public static final String INFO = "\u00A7b";
    public static final String HIGHLIGHT = "\u00A76";
    public static final String SECONDARY = "\u00A77";
    
    public static String getRandomPickupMessage() {
        String[] messages = {
            "Nice catch! That villager is all yours now.",
            "Villager secured! Time to relocate them somewhere better.",
            "Got 'em! This villager is ready for their new home.",
            "Smooth pickup! Your villager collection grows stronger.",
            "Villager captured! They should be easier to move now."
        };
        return messages[(int) (Math.random() * messages.length)];
    }
    
    public static String getRandomPlaceMessage() {
        String[] messages = {
            "Welcome to your new home, little villager!",
            "There we go! Hope they like their new neighborhood.",
            "Villager deployed successfully! Time to let them settle in.",
            "Perfect spot! This villager should be happy here.",
            "Mission accomplished! Another villager finds their place."
        };
        return messages[(int) (Math.random() * messages.length)];
    }
    
    public static String formatMessage(String message) {
        return PREFIX + message;
    }
    
    public static String formatSuccess(String message) {
        return PREFIX + SUCCESS + message;
    }
    
    public static String formatError(String message) {
        return PREFIX + ERROR + message;
    }
    
    public static String formatWarning(String message) {
        return PREFIX + WARNING + message;
    }
    
    public static String formatInfo(String message) {
        return PREFIX + INFO + message;
    }
}
