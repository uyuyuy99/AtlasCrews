package me.uyuyuy99.atlascrews.crew;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.uyuyuy99.atlascrews.Atlas;
import me.uyuyuy99.atlascrews.PlayerData;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrewPlaceholder extends PlaceholderExpansion {

    private final Atlas plugin;

    public CrewPlaceholder(Atlas plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "uyuyuy99";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "atlas";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        Crew crew = playerData.getCrew();

        if (crew == null) return "";

        return crew.getName();
    }

}
