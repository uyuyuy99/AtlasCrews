package me.uyuyuy99.atlascrews.crew;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import me.uyuyuy99.atlascrews.Atlas;
import me.uyuyuy99.atlascrews.Buff;
import me.uyuyuy99.atlascrews.PlayerData;
import me.uyuyuy99.atlascrews.util.CC;
import me.uyuyuy99.atlascrews.util.Util;
import net.minecraft.server.v1_15_R1.ArgumentMobEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;

public class CrewManager {

    private Atlas plugin;

    private File crewsConfigFile;
    private File buffsConfigFile;

    private List<Crew> crewList = new ArrayList<>();
    private List<Buff> buffList = new ArrayList<>();

    public CrewManager(Atlas plugin) {
        this.plugin = plugin;
        crewsConfigFile = new File(plugin.getDataFolder(), "crews.yml");
        buffsConfigFile = new File(plugin.getDataFolder(), "buffs.yml");

        // Give players their potion effects
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                PlayerData playerData = plugin.getPlayerData(p);
                Crew crew = playerData.getCrew();

                if (crew != null) {
                    for (Buff buff : crew.getBuffs()) {
                        if (buff.getType() == Buff.Type.POTION_EFFECT) {
                            p.addPotionEffect(buff.getPotionEffectType().createEffect(400, (int) buff.getModifier()));
                        }
                    }
                }
            }
        }, 20, 20);
    }

    public Crew getCrew(String crewId) {
        for (Crew crew : crewList) {
            if (crew.getId().equals(crewId)) return crew;
        }
        return null;
    }

    public Buff getBuff(String buffId) {
        for (Buff buff : buffList) {
            if (buff.getId().equals(buffId)) return buff;
        }
        return null;
    }

    public List<Crew> getCrews() {
        return crewList;
    }

    // Load buffs & crews from config files, and sets up the GUI
    public void load() {
        // Clear old data
        buffList.clear();
        crewList.clear();

        // Save default buffs.yml and crews.yml
        if (!buffsConfigFile.exists()) {
            plugin.saveResource(buffsConfigFile.getName(), false);
        }
        if (!crewsConfigFile.exists()) {
            plugin.saveResource(crewsConfigFile.getName(), false);
        }

        // Reload buffs & crews configs
        FileConfiguration buffsConfig = YamlConfiguration.loadConfiguration(buffsConfigFile);
        FileConfiguration crewsConfig = YamlConfiguration.loadConfiguration(crewsConfigFile);

        // Looks for defaults in the jar
        Reader defConfigStream1 = new InputStreamReader(plugin.getResource(buffsConfigFile.getName()), StandardCharsets.UTF_8);
        if (defConfigStream1 != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream1);
            buffsConfig.setDefaults(defConfig);
        }
        Reader defConfigStream2 = new InputStreamReader(plugin.getResource(crewsConfigFile.getName()), StandardCharsets.UTF_8);
        if (defConfigStream2 != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream2);
            crewsConfig.setDefaults(defConfig);
        }

        // Load the buffs
        for (String key : buffsConfig.getKeys(false)) {
            ConfigurationSection section = buffsConfig.getConfigurationSection(key);
            Buff buff = new Buff(section.getName());

            buff.setName(CC.format(section.getString("name")));
            buff.setType(Buff.Type.valueOf(section.getString("type").toUpperCase()));
            buff.setModifier(section.getDouble("modifier", 0));

            if (buff.getType() == Buff.Type.POTION_EFFECT) {
                buff.setPotionEffectType(Util.getPotionEFfectTypeFromName(section.getString("potion")));
            }
            if (buff.getType() == Buff.Type.ATTRIBUTE) {
                buff.setAttribute(Attribute.valueOf(section.getString("attribute").replace(".", "_").toUpperCase()));
            }

            buffList.add(buff);
        }

        // Load the crews
        for (String key : crewsConfig.getKeys(false)) {
            ConfigurationSection section = crewsConfig.getConfigurationSection(key);
            Crew crew = new Crew(section.getName());

            crew.setName(CC.format(section.getString("name")));
            crew.setIcon(Util.getIconFromConfig(section, "icon"));

            List<Buff> buffs = new ArrayList<>();
            for (String buffId : section.getStringList("buffs")) {
                Buff buff = getBuff(buffId);

                if (buff == null) {
                    plugin.getLogger().log(Level.SEVERE, "Invalid buff '" + buffId + "' for crew '" + crew.getId() + "'. Check your buffs.yml and crews.yml");
                } else {
                    buffs.add(buff);
                }
            }
            crew.setBuffs(buffs);

            if (section.isSet("crew-specific-eco-items")) crew.setEcoItems(section.getStringList("crew-specific-eco-items"));
            if (section.isSet("crew-specific-eco-armor")) crew.setEcoArmors(section.getStringList("crew-specific-eco-armor"));
            if (section.isSet("crew-specific-pets")) crew.setPets(section.getStringList("crew-specific-pets"));
            if (section.isSet("crew-specific-masks")) crew.setMasks(section.getStringList("crew-specific-masks"));
            if (section.isSet("permission")) crew.setPermission(section.getString("permission"));

            crewList.add(crew);
        }

        // Set up the GUI
        List<String> layout = plugin.getConfig().getStringList("gui.layout");
        ListIterator<String> iter = layout.listIterator();
        char slotChar = 'B';
        while (iter.hasNext()) {
            String line = iter.next();

            while (line.contains("A")) {
                line = line.replaceFirst("A", String.valueOf(slotChar++));
            }
            iter.set(line);
        }
    }

    // Autofill suggestions for commands (lists the available crews)
    public Argument cmdArg(String nodeName) {
        return new CustomArgument<>(nodeName, info -> {
            Crew crew = getCrew(info);

            if (crew == null) {
                throw new CustomArgument.CustomArgumentException("Unknown crew: " + info);
            } else {
                return crew;
            }
        }).overrideSuggestions(info ->
                crewList.stream().map(Crew::getId).toArray(String[]::new)
        );
    }

}
