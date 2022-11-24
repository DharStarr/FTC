package net.forthecrown.core.challenge;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Getter
public class ChallengeDataStorage {
    private static final Logger LOGGER = FTC.getLogger();

    private final Path directory;
    private final Path itemDataDirectory;

    private final Path challengesFile;
    private final Path userDataFile;

    public ChallengeDataStorage(Path directory) {
        this.directory = directory;

        // Directories
        this.itemDataDirectory = directory.resolve("item_data");

        // Files
        this.challengesFile = directory.resolve("challenges.json");
        this.userDataFile = directory.resolve("user_data.json");
    }

    void ensureDefaultsExist() {
        if (!Files.exists(challengesFile)) {
            FTC.getPlugin().saveResource("challenges/challenges.json", false);
            LOGGER.debug("Created challenges.json");
        }
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        try {
            URI jarUri = getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            URI uri = new URI("jar", jarUri.toString(), null);

            try (var system = FileSystems.newFileSystem(uri, env, getClass().getClassLoader())) {
                var path = system.getPath("scripts", "challenges");

                try (var stream = Files.newDirectoryStream(path)) {
                    for (var p: stream) {
                        String name = p.toString();

                        if (Files.exists(PathUtil.pluginPath(name))) {
                            continue;
                        }

                        FTC.getPlugin().saveResource(name, false);
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadChallenges(Registry<Challenge> target) {
        SerializationHelper.readJsonFile(getChallengesFile(), json -> {
            int loaded = 0;

            for (var e: json.entrySet()) {
                if (!Keys.isValidKey(e.getKey())) {
                    LOGGER.warn("Invalid key found: '{}'", e.getKey());
                    continue;
                }

                if (!e.getValue().isJsonObject()) {
                    LOGGER.warn("Expected JSON Object, found: {} in '{}'",
                            e.getValue().getClass(), e.getKey()
                    );

                    continue;
                }

                target.register(
                        e.getKey(),

                        ChallengeParser.parse(e.getValue().getAsJsonObject())
                                .mapError(s -> e.getKey() + ": " + s)
                                .getOrThrow(false, LOGGER::error)
                );
                ++loaded;
            }

            LOGGER.debug("Loaded {} challenges", loaded);
        });
    }

    /* ----------------------------- USER DATA ------------------------------ */

    public DataResult<List<ChallengeEntry>> loadEntries(Registry<Challenge> challenges) {
        return SerializationHelper.readJson(getUserDataFile())
                .map(object -> {
                    List<ChallengeEntry> entries = new ObjectArrayList<>();

                    for (var e: object.entrySet()) {
                        if (!e.getValue().isJsonObject()) {
                            LOGGER.warn(
                                    "Found non object in entry json file under {}",
                                    e.getKey()
                            );

                            continue;
                        }

                        UUID uuid = UUID.fromString(e.getKey());
                        ChallengeEntry entry = new ChallengeEntry(uuid);

                        for (var p: e.getValue().getAsJsonObject().entrySet()) {
                            if (!p.getValue().isJsonPrimitive()
                                    || !((JsonPrimitive) p.getValue()).isNumber()
                            ) {
                                LOGGER.warn("Found non-number in {}'s data", uuid);
                                continue;
                            }

                            float progress = p.getValue()
                                    .getAsNumber()
                                    .floatValue();

                            challenges.get(p.getKey())
                                    .ifPresentOrElse(challenge -> {
                                        entry.getProgress()
                                                .put(challenge, progress);
                                    }, () -> {
                                        LOGGER.warn(
                                                "Unknown challenge {} in {} data",
                                                p.getKey(), uuid
                                        );
                                    });
                        }

                        entries.add(entry);
                    }

                    LOGGER.debug("Loaded {} challenge entries", entries.size());
                    return entries;
                });
    }

    public void saveEntries(Collection<ChallengeEntry> entries,
                            Registry<Challenge> challenges
    ) {
        SerializationHelper.writeJsonFile(getUserDataFile(), wrapper -> {
            for (var e: entries) {
                JsonWrapper json = JsonWrapper.create();

                for (var p: e.getProgress().object2FloatEntrySet()) {
                    if (p.getFloatValue() <= 0) {
                        continue;
                    }

                    challenges.getHolderByValue(p.getKey())
                            .ifPresentOrElse(holder -> {
                                json.add(holder.getKey(), p.getFloatValue());
                            }, () -> {
                                LOGGER.warn(
                                        "Unregistered challenge found in {}",
                                        e.getId()
                                );
                            });
                }

                wrapper.add(e.getId().toString(), json);
            }

            LOGGER.debug("Saved {} challenge entries", wrapper.size());
        });
    }
}