package net.forthecrown.dungeons.level;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.scheduler.BukkitTask;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class LevelManager {
    private static final LevelManager inst = new LevelManager();

    private final Path directory;

    @Getter @Setter
    private DungeonLevel currentLevel;

    private final LevelArchiveStorage archiveStorage;

    private BukkitTask tickTask;
    private final PieceVisitor tickVisitor = new PieceVisitor() {
        @Override
        public Result onGate(DungeonGate gate) {
            gate.onTick();
            return Result.CONTINUE;
        }

        @Override
        public Result onRoom(DungeonRoom room) {
            room.onTick();
            return Result.CONTINUE;
        }
    };

    public LevelManager() {
        this.directory = PathUtil.getPluginDirectory("dungeons");
        archiveStorage = new LevelArchiveStorage(directory.resolve("archive"));
    }

    public static LevelManager get() {
        return inst;
    }

    public Path getLevelPath() {
        return directory.resolve("level.dat");
    }

    public void beginTicking() {
        tickTask = Tasks.cancel(tickTask);
        tickTask = Tasks.runTimer(this::tick, 1, 1);
    }

    public void tick() {
        if (getCurrentLevel() == null) {
            tickTask = Tasks.cancel(tickTask);
            return;
        }

        getCurrentLevel()
                .getRoot()
                .visit(tickVisitor);
    }

    @OnLoad
    public void load() {
        Path levelPath = getLevelPath();

        if (!Files.exists(levelPath)) {
            return;
        }

        DungeonLevel level = new DungeonLevel();
        SerializationHelper.readTagFile(levelPath, level::load);
    }

    @OnSave
    public void save() {
        if (currentLevel == null) {
            PathUtil.safeDelete(getLevelPath());
            return;
        }

        SerializationHelper.writeTagFile(getLevelPath(), currentLevel::save);
    }
}