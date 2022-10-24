package net.forthecrown.dungeons;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.AutoSave;
import net.forthecrown.core.DayChange;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.utils.io.PathUtil;

import java.nio.file.Path;
import java.nio.file.WatchService;

@Getter
public class Dungeons {
    private static final Dungeons inst = new Dungeons();

    private final Path directory;

    @Getter @Setter
    private DungeonLevel currentLevel;

    private final LevelArchiveStorage archiveStorage;

    public Dungeons() {
        this.directory = PathUtil.getPluginDirectory("dungeons");
        archiveStorage = new LevelArchiveStorage(directory.resolve("archive"));
    }

    public static Dungeons get() {
        return inst;
    }

    private static void init() {
        // TODO: load current level, register save callback, and potentially add to day change listener
        get().load();

        AutoSave.get().addCallback(get()::save);
    }

    public void load() {
        Path levelPath = directory.resolve("level.dat");
    }

    public void save() {

    }
}