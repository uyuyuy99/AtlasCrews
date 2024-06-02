package me.uyuyuy99.atlascrews.util;

import org.bukkit.ChatColor;

import java.util.List;

public final class CC {

    public static final String BLACK = ChatColor.BLACK.toString();
    public static final String DARK_BLUE = ChatColor.DARK_BLUE.toString();
    public static final String DARK_GREEN = ChatColor.DARK_GREEN.toString();
    public static final String DARK_AQUA = ChatColor.DARK_AQUA.toString();
    public static final String DARK_RED = ChatColor.DARK_RED.toString();
    public static final String DARK_PURPLE = ChatColor.DARK_PURPLE.toString();
    public static final String GOLD = ChatColor.GOLD.toString();
    public static final String GRAY = ChatColor.GRAY.toString();
    public static final String DARK_GRAY = ChatColor.DARK_GRAY.toString();
    public static final String BLUE = ChatColor.BLUE.toString();
    public static final String GREEN = ChatColor.GREEN.toString();
    public static final String AQUA = ChatColor.AQUA.toString();
    public static final String RED = ChatColor.RED.toString();
    public static final String LIGHT_PURPLE = ChatColor.LIGHT_PURPLE.toString();
    public static final String YELLOW = ChatColor.YELLOW.toString();
    public static final String WHITE = ChatColor.WHITE.toString();
    public static final String BOLD = ChatColor.BOLD.toString();
    public static final String STRIKE = ChatColor.STRIKETHROUGH.toString();
    public static final String UNDERLINE = ChatColor.UNDERLINE.toString();
    public static final String MAGIC = ChatColor.MAGIC.toString();
    public static final String ITALIC = ChatColor.ITALIC.toString();
    public static final String RESET = ChatColor.RESET.toString();


    /**
     * Format a string by converting color symbols into actual colors.
     * @param string String to format.
     */
    public static String format(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }


    /**
     * Format a list of strings by converting color symbols into actual colors.
     * @param list List of Strings to format.
     */
    public static List<String> format(List<String> list) {
        list.replaceAll(CC::format);
        return list;
    }


    /**
     * Strip a formatted string of its formatting.
     * @param string String to clear its format.
     */
    public static String clearFormat(String string) {
        return ChatColor.stripColor(string);
    }

}
