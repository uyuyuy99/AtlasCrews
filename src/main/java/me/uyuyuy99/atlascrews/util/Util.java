package me.uyuyuy99.atlascrews.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.uyuyuy99.atlascrews.Atlas;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.*;

public class Util {

    private static Map<String, PotionEffectType> potionNameMap = new HashMap<String, PotionEffectType>() {{
       put("slowness", PotionEffectType.SLOW);
       put("haste", PotionEffectType.FAST_DIGGING);
       put("mining_fatigue", PotionEffectType.SLOW_DIGGING);
       put("strength", PotionEffectType.INCREASE_DAMAGE);
       put("instant_health", PotionEffectType.HEAL);
       put("instant_damage", PotionEffectType.HARM);
       put("jump_boost", PotionEffectType.JUMP);
       put("nausea", PotionEffectType.CONFUSION);
       put("resistance", PotionEffectType.DAMAGE_RESISTANCE);
    }};

    public static PotionEffectType getPotionEFfectTypeFromName(String name) {
        PotionEffectType type = PotionEffectType.getByName(name);

        if (type != null) {
            return type;
        }

        return potionNameMap.get(name.toLowerCase());
    }

    public static void addGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 69);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public static void setHeadTexture(ItemStack head, String url) {
        if(url.isEmpty()) {
            return;
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", "http://textures.minecraft.net/texture/" + url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));

        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

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
