package net.forthecrown.vikings.valhalla;

import com.google.gson.*;
import net.forthecrown.core.utils.Pair;
import net.forthecrown.vikings.utils.VikingUtils;
import net.forthecrown.vikings.valhalla.data.*;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import static net.forthecrown.core.utils.JsonUtils.*;

public class RaidSerializer implements JsonSerializer<VikingRaid>, JsonDeserializer<VikingRaid> {

    @Override
    public VikingRaid deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = element.getAsJsonObject();

        Key name = Key.key(json.get("name").getAsString());
        Location loc = deserializeLocation(json.get("startLocation").getAsJsonObject());
        BoundingBox region = VikingUtils.deserializeBukkitRegion(json.get("region").getAsJsonObject());

        VikingRaid result = new VikingRaid(name, loc, region);

        if(json.has("scores")){
            Map<UUID, Integer> scores = deserializeMap(json.get("scores").getAsJsonObject(),
                    e -> new Pair<>(UUID.fromString(e.getKey()), e.getValue().getAsInt())
            );

            result.scoreMap = scores;
        }

        if(json.has("generationData")){
            JsonObject gen = json.getAsJsonObject("generationData");

            RaidGenerationData generation = new RaidGenerationData(result);
            result.generatorData = generation;

            if(gen.has("lootData")) generation.lootData = new LootData(gen.get("lootData"));
            if(gen.has("worldData")) generation.worldData = new WorldData(gen.get("worldData"));
            if(gen.has("mobData")) generation.mobData = new MobData(gen.get("mobData"));
            if(gen.has("triggerData")) generation.triggerData = new TriggerData(gen.get("triggerData"));
        }

        return result;
    }

    @Override
    public JsonObject serialize(VikingRaid raid, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.add("name", new JsonPrimitive(raid.key.asString()));
        json.add("startLocation", serializeLocation(raid.getStartLocation()));
        json.add("region", VikingUtils.serializeBukkitRegion(raid.region));

        if(raid.scoreMap.size() > 0) {
            json.add("scores", serializeMap(
                    raid.scoreMap,
                    e -> new Pair<>(e.getKey().toString(),new JsonPrimitive(e.getValue()) ))
            );
        }

        if(raid.generatorData == null) return json;

        RaidGenerationData data = raid.generatorData;
        JsonObject gen = new JsonObject();

        //if(data.lootData != null) gen.add("lootData", data.lootData.serialize());
        JsonObject lootData = data.lootData == null ? null : data.lootData.serialize();
        if(lootData != null) gen.add("lootData", lootData);

        if(data.worldData != null) gen.add("worldData", data.worldData.serialize());
        if(data.mobData != null) gen.add("mobData", data.mobData.serialize());
        if(data.triggerData != null) gen.add("triggerData", data.triggerData.serialize());

        json.add("generationData", gen);
        return json;
    }
}