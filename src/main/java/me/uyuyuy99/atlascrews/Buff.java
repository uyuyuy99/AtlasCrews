package me.uyuyuy99.atlascrews;

import lombok.Data;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;

@Data
public class Buff {

    public enum Type {

        POTION_EFFECT,
        ATTRIBUTE,
        SWORD_DAMAGE,
        BOW_DAMAGE

    }

    private String id;
    private String name;
    private Type type;
    private double modifier;
    private PotionEffectType potionEffectType;
    private Attribute attribute;

    public Buff(String id) {
        this.id = id;
    }

}
