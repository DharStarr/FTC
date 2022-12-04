package net.forthecrown.dungeons.level;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class LevelManager {
    @Getter
    private static final LevelManager instance = new LevelManager();

    @Getter @Setter
    private DungeonLevel currentLevel;

    private final LevelDataStorage storage;

    public LevelManager() {
        storage = new LevelDataStorage(
                PathUtil.getPluginDirectory("dungeons")
        );
    }

    @OnLoad
    public void load() {
        Path levelPath = storage.getActiveLevel();

        if (!Files.exists(levelPath)) {
            return;
        }

        DungeonLevel level = new DungeonLevel();
        SerializationHelper.readTagFile(levelPath, level::load);
    }

    @OnSave
    public void save() {
        if (currentLevel == null) {
            PathUtil.safeDelete(storage.getActiveLevel());
            return;
        }

        SerializationHelper.writeTagFile(
                storage.getActiveLevel(),
                currentLevel::save
        );
    }
}