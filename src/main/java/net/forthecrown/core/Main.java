package net.forthecrown.core;

import net.forthecrown.dungeons.Bosses;
import net.forthecrown.events.MobHealthBar;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.utils.world.WorldLoader;
import net.kyori.adventure.key.Namespaced;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.forthecrown.utils.Util.runSafe;

/**
 * Main class that does all the dirty internal stuff
 */
public final class Main extends JavaPlugin implements Namespaced {
    public static final String
            NAME            = "ForTheCrown",
            NAMESPACE       = NAME.toLowerCase(),
            OLD_NAMESPACE   = "ftccore";

    boolean debugMode;
    FtcLogger logger;

    @Override
    public void onEnable() {
        // Register dynmap hook connection thing
        DynmapUtil.registerListener();

        setDebugMode();
        ensureLoggerExists();

        BootStrap.init();

        FTC.getLogger().info("FTC started");
    }

    @Override
    public void onLoad() {
        setDebugMode();
        ensureLoggerExists();

        FtcFlags.init();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        AutoSave.get().run();

        runSafe(MobHealthBar::shutdown);
        runSafe(Bosses::shutdown);
        runSafe(WorldLoader::shutdown);
        runSafe(PacketListeners::removeAll);
    }

    private void ensureLoggerExists() {
        if (logger != null) {
            return;
        }

        logger = new FtcLogger(
                (ExtendedLogger) LogManager.getLogger(getLogger().getName())
        );
    }

    private void setDebugMode() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(
                getTextResource("plugin.yml")
        );

        debugMode = config.getBoolean("debug_build");
    }

    @Override
    public @NonNull String namespace() {
        return NAMESPACE;
    }
}