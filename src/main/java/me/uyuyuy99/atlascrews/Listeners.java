package me.uyuyuy99.atlascrews;

import me.uyuyuy99.atlascrews.crew.Crew;
import me.uyuyuy99.atlascrews.event.armor.ArmorEquipEvent;
import me.uyuyuy99.atlascrews.util.CC;
import me.uyuyuy99.atlascrews.util.PersistentUtils;
import net.splodgebox.elitemasks.EliteAPI;
import net.splodgebox.elitepets.ElitePetsAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.util.logging.Level;

public class Listeners implements Listener {

    private Atlas plugin;

    public Listeners(Atlas plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.loadPlayerData(player);
//        plugin.removeAttributeModifiers(player);
//        plugin.applyAttributeModifiers(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

//        plugin.removeAttributeModifiers(player);
        plugin.savePlayerData(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (item == null) return;

        if (PersistentUtils.hasKey(item, Atlas.CREW_TOKEN_KEY)) {
            Crew crew = plugin.crews().getCrew(PersistentUtils.getKey(item, Atlas.CREW_TOKEN_KEY));

            if (crew != null && action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                PlayerData playerData = plugin.getPlayerData(player);

                if (crew.equals(playerData.getCrew())) {
                    player.sendMessage(CC.format(plugin.getConfig().getString("messages.already-have-crew")
                            .replace("%crew%", crew.getName())));
                } else if (playerData.performPetCheck(player)) {
                    playerData.removeEcoArmorAndMasks();
                    playerData.setCrew(crew);
                    plugin.removeAttributeModifiers(player);
                    plugin.applyAttributeModifiers(player);
                    player.sendMessage(CC.format(plugin.getConfig().getString("messages.changed-crew")
                            .replace("%crew%", crew.getName())));
                    item.setAmount(item.getAmount() - 1);
                }
            }
        }

        if (PersistentUtils.hasKey(item, Atlas.ECO_ITEM_KEY)) {
            PlayerData playerData = plugin.getPlayerData(player);
            Crew crewNeeded = playerData.crewNeededForEcoItem(PersistentUtils.getKey(item, Atlas.ECO_ITEM_KEY));

            if (crewNeeded != null) {
                event.setCancelled(true);
                player.sendMessage(CC.format(plugin.getConfig().getString("messages.cant-use-eco-item")
                        .replace("%crew%", crewNeeded.getName())));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEquipArmor(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        ItemStack armor = event.getNewArmorPiece();
        if (armor == null) return;

        if (PersistentUtils.hasKey(armor, Atlas.ECO_ARMOR_KEY)) {
            PlayerData playerData = plugin.getPlayerData(player);
            Crew crewNeeded = playerData.crewNeededForEcoArmor(PersistentUtils.getKey(armor, Atlas.ECO_ARMOR_KEY));

            if (crewNeeded != null) {
                event.setCancelled(true);
                player.sendMessage(CC.format(plugin.getConfig().getString("messages.cant-use-eco-armor")
                        .replace("%crew%", crewNeeded.getName())));
            }
        }

        if (EliteAPI.hasMask(armor)) {
            PlayerData playerData = plugin.getPlayerData(player);
            Crew crewNeeded = playerData.crewNeededForMask(EliteAPI.getMaskName(armor));

            if (crewNeeded != null) {
                event.setCancelled(true);
                player.sendMessage(CC.format(plugin.getConfig().getString("messages.cant-use-mask")
                        .replace("%crew%", crewNeeded.getName())));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (PersistentUtils.hasKey(itemInHand, Atlas.ECO_ITEM_KEY)) {
                PlayerData playerData = plugin.getPlayerData(player);
                Crew crewNeeded = playerData.crewNeededForEcoItem(PersistentUtils.getKey(itemInHand, Atlas.ECO_ITEM_KEY));

                if (crewNeeded != null) {
                    event.setCancelled(true);
                    player.sendMessage(CC.format(plugin.getConfig().getString("messages.cant-use-eco-item")
                            .replace("%crew%", crewNeeded.getName())));
                    return;
                }
            }
            if (itemInHand.getType().name().contains("_SWORD")) {
                PlayerData playerData = plugin.getPlayerData(player);
                Crew crew = playerData.getCrew();

                if (crew != null) {
                    for (Buff buff : crew.getBuffs()) {
                        if (buff.getType() == Buff.Type.SWORD_DAMAGE) {
                            event.setDamage(event.getDamage() * (1d + (buff.getModifier() / 100)));
                        }
                    }
                }
            }
        }
        if (event.getDamager() instanceof Arrow) {
            ProjectileSource shooter = ((Arrow) event.getDamager()).getShooter();

            if (shooter instanceof Player) {
                Player player = (Player) shooter;
                PlayerData playerData = plugin.getPlayerData(player);
                Crew crew = playerData.getCrew();

                if (crew != null) {
                    for (Buff buff : crew.getBuffs()) {
                        if (buff.getType() == Buff.Type.BOW_DAMAGE) {
//                            System.out.println("Modifier: " + (1d + (buff.getModifier() / 100)));
//                            System.out.println("Before: " + event.getDamage());
                            event.setDamage(event.getDamage() * (1d + (buff.getModifier() / 100)));
//                            System.out.println("After: " + event.getDamage());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (ElitePetsAPI.getPetAPI().isPet(item)) {
            PlayerData playerData = plugin.getPlayerData(player);
            Crew crewNeeded = playerData.crewNeededForPet(ElitePetsAPI.getPetAPI().getPet(item).getName());

            if (crewNeeded != null) {
                event.setCancelled(true);
                player.sendMessage(CC.format(plugin.getConfig().getString("messages.cant-use-pet")
                        .replace("%crew%", crewNeeded.getName())));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        if (ElitePetsAPI.getPetAPI().isPet(item)) {
            PlayerData playerData = plugin.getPlayerData(player);
            Crew crewNeeded = playerData.crewNeededForPet(ElitePetsAPI.getPetAPI().getPet(item).getName());

            if (crewNeeded != null) {
                event.setCancelled(true);
            }
        }
    }

}
