package me.uyuyuy99.atlascrews.crew;

import com.google.gson.*;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import lombok.Data;
import me.uyuyuy99.atlascrews.Atlas;
import me.uyuyuy99.atlascrews.Buff;
import me.uyuyuy99.atlascrews.util.PersistentUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Data
public class Crew {

    private String id;
    private String name;
    private ItemStack icon;
    private List<Buff> buffs = new ArrayList<>();
    private List<String> ecoItems = new ArrayList<>();
    private List<String> ecoArmors = new ArrayList<>();
    private String permission;

    public Crew(String id) {
        this.id = id;
    }

    public boolean hasEcoItem(String ecoItem) {
        return ecoItems.contains(ecoItem);
    }

    public boolean hasEcoArmor(String ecoArmor) {
        return ecoArmors.contains(ecoArmor);
    }

    public static class Serializer implements JsonSerializer<Crew>, JsonDeserializer<Crew> {

        @Override
        public JsonElement serialize(Crew crew, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("id", crew.getId());
            return object;
        }

        @Override
        public Crew deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Atlas.plugin().crews().getCrew(json.getAsJsonObject().get("id").getAsString());
        }

    }

}
