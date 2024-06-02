package me.uyuyuy99.atlascrews.util;

import me.uyuyuy99.atlascrews.Atlas;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public class Util {

    public static void addGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 69);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public static void setHeadTexture(ItemStack head, String url) {
        if (url.isEmpty()) {
            return;
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

        try {
            profile.getTextures().setSkin(URI.create("http://textures.minecraft.net/texture/" + url).toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        headMeta.setOwnerProfile(profile);
        head.setItemMeta(headMeta);
    }

    public static void setItemName(ItemStack item, String name) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(CC.format(name));
        item.setItemMeta(itemMeta);
    }

    public static void setItemLore(ItemStack item, List<String> lore) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(CC.format(lore));
        item.setItemMeta(itemMeta);
    }

    public static void hideItemAttributes(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(itemMeta);
        }
    }

    public static ItemStack getIconFromConfig(ConfigurationSection section, String key) {
        ItemStack item = new ItemStack(
                Material.valueOf(section.getString(key).toUpperCase()),
                section.getInt(key + "-amount", 1)
        );

        if (section.isSet(key + "-name")) {
            setItemName(item, section.getString(key + "-name"));
        }
        if (section.isSet(key + "-lore")) {
            setItemLore(item, section.getStringList(key + "-lore"));
        }
        if (section.getBoolean(key + "-glow", false)) {
            addGlow(item);
        }
        if (item.getType() == Material.PLAYER_HEAD && section.isSet(key + "-head-url")) {
            setHeadTexture(item, section.getString(key + "-head-url"));
        }

        hideItemAttributes(item);
        return item;
    }
    public static ItemStack getIconFromConfig(FileConfiguration config, String key) {
        return getIconFromConfig(config.getRoot(), key);
    }
    public static ItemStack getIconFromConfig(String key) {
        return getIconFromConfig(Atlas.plugin().getConfig(), key);
    }

}
