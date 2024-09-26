package me.uyuyuy99.atlascrews;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.uyuyuy99.atlascrews.crew.Crew;
import me.uyuyuy99.atlascrews.crew.CrewManager;
import me.uyuyuy99.atlascrews.crew.CrewPlaceholder;
import me.uyuyuy99.atlascrews.event.armor.ArmorListener;
import me.uyuyuy99.atlascrews.event.armor.DispenserArmorListener;
import me.uyuyuy99.atlascrews.util.CC;
import me.uyuyuy99.atlascrews.util.Json;
import me.uyuyuy99.atlascrews.util.PersistentUtils;
import me.uyuyuy99.atlascrews.util.Util;
import net.splodgebox.elitepets.ElitePetsAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class Atlas extends JavaPlugin {

    private static Atlas PLUGIN;
    public static File PLAYER_FOLDER;
    public static NamespacedKey CREW_TOKEN_KEY;
    public static NamespacedKey ECO_ITEM_KEY;
    public static NamespacedKey ECO_ARMOR_KEY;

    private Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private CrewManager crews;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIConfig());
    }

    @Override
    public void onEnable() {
        PLUGIN = this;
        CREW_TOKEN_KEY = new NamespacedKey(this, "atlas_token_crew");
//        ECO_ITEM_KEY = new NamespacedKey(getServer().getPluginManager().getPlugin("EcoItems"), "item");
//        ECO_ARMOR_KEY = new NamespacedKey(getServer().getPluginManager().getPlugin("EcoArmor"), "set");
        ECO_ITEM_KEY = new NamespacedKey(this, "eco_items_item"); //TODO
        ECO_ARMOR_KEY = new NamespacedKey(this, "eco_armor_set"); //TODO

        // ArmorEquipEvent - https://github.com/Arnuh/ArmorEquipEvent
        getServer().getPluginManager().registerEvents(new ArmorListener(new ArrayList<>()), this);
        try {
            Class.forName("org.bukkit.event.block.BlockDispenseArmorEvent");
            getServer().getPluginManager().registerEvents(new DispenserArmorListener(), this);
        } catch (Exception ignored) {}

        // Create folder for player data
        PLAYER_FOLDER = new File(getDataFolder(), "players");
        if (PLAYER_FOLDER.mkdirs()) {
            getLogger().log(Level.INFO, "Created players folder");
        }

        // Load config/data files files
        crews = new CrewManager(this);
        crews.load();
        saveDefaultConfig();
        for (Player p : getServer().getOnlinePlayers()) {
            loadPlayerData(p);
//            removeAttributeModifiers(p);
//            applyAttributeModifiers(p);
        }

        // Register the placeholder for crew names
        new CrewPlaceholder(this).register();

        // Listeners
        getServer().getPluginManager().registerEvents(new Listeners(this), this);

        // Commands
        new CommandAPICommand("atlas")
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission("atlas.reload")
                        .executes((sender, args) -> {
                            reload();
                            sender.sendMessage(CC.GREEN + "Atlas plugin reloaded!");
                        })
                )
                .withSubcommand(new CommandAPICommand("set")
                        .withArguments(new PlayerArgument("player"))
                        .withArguments(crews.cmdArg("crew"))
                        .withPermission("atlas.set")
                        .executes((sender, args) -> {
                            Player player = (Player) args[0];
                            PlayerData playerData = getPlayerData(player);

                            // Make player drop crew-specific pets before they change
                            if (!playerData.performPetCheck(player, sender)) {
                                return;
                            }

                            playerData.removeEcoArmorAndMasks();
                            playerData.setCrew((Crew) args[1]);
                            removeAttributeModifiers(player);
                            applyAttributeModifiers(player);

                            player.sendMessage(CC.format(getConfig().getString("messages.changed-crew")
                                    .replace("%crew%", playerData.getCrew().getName())));
                            sender.sendMessage(CC.format(getConfig().getString("messages.changed-other-crew")
                                    .replace("%player%", player.getName())
                                    .replace("%crew%", playerData.getCrew().getName())));
                        })
                )
                .withSubcommand(new CommandAPICommand("reset")
                        .withArguments(new PlayerArgument("player"))
                        .withPermission("atlas.reset")
                        .executes((sender, args) -> {
                            Player player = (Player) args[0];
                            PlayerData playerData = getPlayerData(player);

                            // Make player drop crew-specific pets before they change
                            if (!playerData.performPetCheck(player, sender)) {
                                return;
                            }

                            playerData.removeEcoArmorAndMasks();
                            playerData.setCrew(null);
                            removeAttributeModifiers(player);

                            player.sendMessage(CC.format(getConfig().getString("messages.reset-crew")));
                            sender.sendMessage(CC.format(getConfig().getString("messages.reset-other-crew")
                                    .replace("%player%", player.getName())));
                        })
                )
                .withSubcommand(new CommandAPICommand("givetoken")
                        .withArguments(new PlayerArgument("player"))
                        .withArguments(crews.cmdArg("crew"))
                        .withPermission("atlas.givetoken")
                        .executes(this::runGiveTokenCommand)
                )
                .withSubcommand(new CommandAPICommand("givetoken")
                        .withArguments(new PlayerArgument("player"))
                        .withArguments(crews.cmdArg("crew"))
                        .withArguments(new IntegerArgument("amount", 1, 64))
                        .withPermission("atlas.givetoken")
                        .executes(this::runGiveTokenCommand)
                )
                .register();
    }

    @Override
    public void onDisable() {
//        for (Player p : getServer().getOnlinePlayers()) {
//            removeAttributeModifiers(p);
//        }
        saveAllPlayerData();
    }

    public void reload() {
        reloadConfig();
        crews.load();
    }

    public static Atlas plugin() {
        return PLUGIN;
    }

    public CrewManager crews() {
        return crews;
    }

    private void runGiveTokenCommand(CommandSender sender, Object[] args) {
        Player player = (Player) args[0];
        Crew crew = (Crew) args[1];
        int amount = 1;
        if (args.length > 2) {
            amount = (int) args[2];
        }

        ItemStack token = new ItemStack(Material.valueOf(getConfig().getString("options.crew-item").toUpperCase()), amount);
        Util.setItemName(token, getConfig().getString("options.crew-item-name").replace("%crew%", crew.getName()));
        PersistentUtils.setKey(token, CREW_TOKEN_KEY, crew.getId());

        List<String> lore = new ArrayList<>();
        for (String s : getConfig().getStringList("options.crew-item-lore-prefix")) {
            lore.add(s.replace("%crew%", crew.getName()));
        }
        for (Buff buff : crew.getBuffs()) {
            lore.add(getConfig().getString("options.crew-item-lore-buff").replace("%buff%", buff.getName()));
        }
        for (String s : getConfig().getStringList("options.crew-item-lore-suffix")) {
            lore.add(s.replace("%crew%", crew.getName()));
        }
        Util.setItemLore(token, lore);

        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(token);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItem(player.getLocation(), leftover);
        }

        sender.sendMessage(CC.format(getConfig().getString("messages.gave-token")
                .replace("%player%", player.getName())
                .replace("%crew%", crew.getName())));
    }

    public void applyAttributeModifiers(Player player) {
        PlayerData playerData = getPlayerData(player);
        if (playerData.getCrew() == null) return;

        for (Buff buff : playerData.getCrew().getBuffs()) {
            if (buff.getType() == Buff.Type.ATTRIBUTE) {
                AttributeInstance attribute = player.getAttribute(buff.getAttribute());
                UUID uuid = UUID.randomUUID();
                attribute.addModifier(new AttributeModifier(uuid, "Atlas Buff", buff.getModifier(), AttributeModifier.Operation.ADD_NUMBER));
                playerData.addAttributeMod(uuid);
            }
        }

        playerData.save();
    }

    public void removeAttributeModifiers(Player player) {
        PlayerData playerData = getPlayerData(player);

        for (Attribute attr : Attribute.values()) {
            AttributeInstance playerAttr = player.getAttribute(attr);
            if (playerAttr == null) continue;

            for (AttributeModifier attrMod : playerAttr.getModifiers()) {
                if (playerData.getAttributeMods().contains(attrMod.getUniqueId())) {
                    playerAttr.removeModifier(attrMod);
                }
            }
        }

        playerData.setAttributeMods(new ArrayList<>());
    }

    ///////////////////////////////////////////////
    /////////////                   ///////////////
    /////////////    Player Data    ///////////////
    /////////////                   ///////////////
    ///////////////////////////////////////////////

    public PlayerData getPlayerData(UUID uuid) {
        if (playerDataMap.get(uuid) == null) {
            loadPlayerData(uuid);
        }
        return playerDataMap.get(uuid);
    }
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void loadPlayerData(UUID uuid) {
        File file = new File(PLAYER_FOLDER, uuid + ".json");
        PlayerData data = Json.load(file, PlayerData.class, PlayerData::new);
        data.setUuid(uuid);
        playerDataMap.put(uuid, data);
    }
    public void loadPlayerData(Player player) {
        loadPlayerData(player.getUniqueId());
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);

        if (data != null) {
            data.save();
        }
    }
    public void savePlayerData(Player player) {
        savePlayerData(player.getUniqueId());
    }

    public void saveAllPlayerData() {
        for (PlayerData data : playerDataMap.values()) {
            data.save();
        }
    }

}
