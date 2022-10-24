package net.forthecrown.utils.io;

import com.google.gson.JsonObject;
import net.forthecrown.core.FTC;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class SerializationHelper {
    private SerializationHelper() {}

    private static final Logger LOGGER = FTC.getLogger();

    public static final IoReader<CompoundTag> TAG_READER = file -> NbtIo.readCompressed(Files.newInputStream(file));
    public static final IoReader<JsonObject> JSON_READER = JsonUtils::readFileObject;

    public static <T> boolean readFile(Path file, IoReader<T> reader, Consumer<T> loadCallback) {
        if (!Files.exists(file)) {
            return false;
        }

        try {
            var t = reader.apply(file);
            loadCallback.accept(t);
        } catch (IOException e) {
            LOGGER.error("Error writing file: '" + file + "'", e);
            return false;
        }

        return true;
    }

    public static boolean readTagFile(Path file, Consumer<CompoundTag> loadCallback) {
        return readFile(file, TAG_READER, loadCallback);
    }

    public static boolean readJsonFile(Path file, Consumer<JsonWrapper> loadCallback) {
        return readFile(file, JSON_READER, object -> loadCallback.accept(JsonWrapper.wrap(object)));
    }

    public static void writeFile(Path file, IoWriter writer) {
        try {
            if (!Files.exists(file)) {
                var parent = file.getParent();

                if (!Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
            }

            writer.apply(file);
        } catch (IOException e) {
            LOGGER.error("Error writing file: '" + file + "'", e);
        }
    }

    public static void writeTagFile(Path f, Consumer<CompoundTag> saveCallback) {
        writeFile(f, file -> {
            var tag = new CompoundTag();
            saveCallback.accept(tag);
            NbtIo.writeCompressed(tag, Files.newOutputStream(file));
        });
    }

    public static void writeJsonFile(Path f, Consumer<JsonWrapper> saveCallback) {
        writeFile(f, file -> {
            var json = JsonWrapper.create();
            saveCallback.accept(json);
            JsonUtils.writeFile(json.getSource(), file);
        });
    }

    public interface IoReader<O> {
        O apply(Path file) throws IOException;
    }

    public interface IoWriter {
        void apply(Path file) throws IOException;
    }
}