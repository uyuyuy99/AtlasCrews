package me.uyuyuy99.atlascrews;

import lombok.Data;
import me.uyuyuy99.atlascrews.crew.Crew;
import me.uyuyuy99.atlascrews.util.CC;
import me.uyuyuy99.atlascrews.util.Json;
import me.uyuyuy99.atlascrews.util.PersistentUtils;
import net.splodgebox.elitemasks.EliteAPI;
import net.splodgebox.elitepets.ElitePetsAPI;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
public class PlayerData {

    private transient UUID uuid;

    private Crew crew;
    private List<UUID> attributeMods = new ArrayList<>();

    public void save() {
        File file = new File(Atlas.PLAYER_FOLDER, uuid + ".json");
        Json.save(file, this);
    }

    public void addAttributeMod(UUID uuid) {
        attributeMods.add(uuid);
    }

    // Returns NULL if player can use the ecoItem, else returns the crew needed to use it
    public Crew crewNeededForEcoItem(String ecoItem) {
        // If the player's crew lists it as a crew-specific eco item, then they can use it
        if (crew != null && crew.hasEcoItem(ecoItem)) {
            return null;
        }
        // Otherwise, if any other crews have it as an eco item, then they can't use it
        for (Crew c : Atlas.plugin().crews().getCrews()) {
            if (c.hasEcoItem(ecoItem)) {
                return c;
            }
        }
        return null;
    }

    // Returns NULL if player can use the ecoArmor, else returns the crew needed to use it
    public Crew crewNeededForEcoArmor(String ecoArmor) {
        // If the player's crew lists it as a crew-specific eco armor, then they can use it
        if (crew != null && crew.hasEcoArmor(ecoArmor)) {
            return null;
        }
        // Otherwise, if any other crews have it as an eco armor, then they can't use it
        for (Crew c : Atlas.plugin().crews().getCrews()) {
            if (c.hasEcoArmor(ecoArmor)) {
                return c;
            }
        }
        return null;
    }

    // Returns NULL if player can use the pet, else returns the crew needed to use it
    public Crew crewNeededForPet(String pet) {
        // If the player's crew lists it as a crew-specific pet, then they can use it
        if (crew != null && crew.hasPet(pet)) {
            return null;
        }
        // Otherwise, if any other crews have it as a pet, then they can't use it
        for (Crew c : Atlas.plugin().crews().getCrews()) {
            if (c.hasPet(pet)) {
                return c;
            }
        }
        return null;
    }

    // Returns NULL if player can use the mask, else returns the crew needed to use it
    public Crew crewNeededForMask(String mask) {
        // If the player's crew lists it as a crew-specific mask, then they can use it
        if (crew != null && crew.hasMask(mask)) {
            return null;
        }
        // Otherwise, if any other crews have it as a mask, then they can't use it
        for (Crew c : Atlas.plugin().crews().getCrews()) {
            if (c.hasMask(mask)) {
                return c;
            }
        }
        return null;
    }

    // If player is wearing eco armor or mask specific to his crew, it will remove it and place in his inventory
    public void removeEcoArmorAndMasks() {
        if (crew == null) return;

        Player player = Atlas.plugin().getServer().getPlayer(uuid);
        if (player == null) return;

        PlayerInventory inv = player.getInventory();
        ItemStack[] armorContents = inv.getArmorContents();
        List<ItemStack> returnToInv = new ArrayList<>();

        for (int i=0; i<armorContents.length; i++) {
            ItemStack armorPiece = armorContents[i];
            if (armorPiece == null) continue;

            if (PersistentUtils.hasKey(armorPiece, Atlas.ECO_ARMOR_KEY)) {
                String ecoArmorPiece = PersistentUtils.getKey(armorPiece, Atlas.ECO_ARMOR_KEY);

                if (crew.hasEcoArmor(ecoArmorPiece)) {
                    returnToInv.add(armorPiece);
                    armorContents[i] = new ItemStack(Material.AIR);
                }
            }
            else if (EliteAPI.hasMask(armorPiece)) {
                String mask = EliteAPI.getMaskName(armorPiece);

                if (crew.hasMask(mask)) {
                    returnToInv.add(armorPiece);
                    armorContents[i] = new ItemStack(Material.AIR);
                }
            }
        }

        inv.setArmorContents(armorContents);

        // Give the armor back to the player, or drop it at his feet if inv is full
        HashMap<Integer, ItemStack> leftovers = inv.addItem(returnToInv.toArray(new ItemStack[0]));
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItem(player.getLocation(), leftover);
        }
    }

    // Checks if user has pets in inventory that prevent him from changing crews
    // Returns TRUE if user can safely change crews
    public boolean performPetCheck(Player player, CommandSender sender) {
        if (crew != null) {
            for (ItemStack i : player.getInventory().getContents()) {
                if (ElitePetsAPI.getPetAPI().isPet(i) && crew.hasPet(ElitePetsAPI.getPetAPI().getPet(i).getName())) {
                    player.sendMessage(CC.format(Atlas.plugin().getConfig().getString("messages.remove-your-pets")
                            .replace("%crew%", crew.getName())));

                    if (sender != null && (player != sender)) {
                        sender.sendMessage(CC.format(Atlas.plugin().getConfig().getString("messages.remove-their-pets")
                                .replace("%player%", player.getName())
                                .replace("%crew%", crew.getName())));
                    }

                    return false;
                }
            }
        }
        return true;
    }
    public boolean performPetCheck(Player player) {
        return performPetCheck(player, null);
    }

}
