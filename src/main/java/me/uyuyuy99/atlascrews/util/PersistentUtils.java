package me.uyuyuy99.atlascrews.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PersistentUtils {

    public static boolean hasKey(ItemStack itemStack, NamespacedKey key) {
        if (itemStack.getType() == Material.AIR)
            return false;

        if (!itemStack.hasItemMeta())
            return false;

        ItemMeta itemMeta = itemStack.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        return dataContainer.has(key, PersistentDataType.STRING);
    }

    public static String getKey(ItemStack itemStack, NamespacedKey key) {
        if (!hasKey(itemStack, key))
            return null;

        ItemMeta itemMeta = itemStack.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        return dataContainer.get(key, PersistentDataType.STRING);
    }

    public static void setKey(ItemStack itemStack, NamespacedKey key, String value) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        dataContainer.set(key, PersistentDataType.STRING, value);

        itemStack.setItemMeta(itemMeta);
    }

}
