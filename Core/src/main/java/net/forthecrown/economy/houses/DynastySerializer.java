package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;

import java.io.File;
import java.io.IOException;

public final class DynastySerializer {
    private DynastySerializer() {}

    public static void serialize() {
        JsonWrapper json = JsonWrapper.empty();

        for (Dynasty h: Registries.DYNASTIES) {
            json.add(h.toString(), h.serialize());
        }

        try {
            JsonUtils.writeFile(json.getSource(), getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deserialize() {
        JsonWrapper json;

        try {
            json = JsonWrapper.of(JsonUtils.readFile(getFile()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (Dynasty h: Registries.DYNASTIES) {
            JsonElement element = json.get(h.toString());
            h.deserialize(element == null ? new JsonObject() : element);
        }
    }

    private static File getFile() {
        File file = new File(Crown.dataFolder(), "houses.json");

        if(file.isDirectory()) file.delete();
        if(!file.exists()) Crown.saveResource(true, "houses.json");

        return file;
    }
}
