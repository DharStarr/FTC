package net.forthecrown.core;

import net.forthecrown.dungeons.Bosses;
import net.forthecrown.events.MobHealthBar;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.utils.world.WorldLoader;
import net.kyori.adventure.key.Namespaced;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dynmap.DynmapCommonAPIListener;

import static net.forthecrown.core.FtcDiscord.C_SERVER;
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

    @Override
    public void onEnable() {
        // Register dynmap hook connection thing
        DynmapCommonAPIListener.register(new FtcDynmap());

        setDebugMode();
        BootStrap.init();

        FtcDiscord.staffLog(C_SERVER, "FTC started, plugin version: {}, paper version: {}",
                getDescription().getVersion(),
                Bukkit.getVersion()
        );

        FTC.getLogger().info("FTC started");
    }

    @Override
    public void onLoad() {
        setDebugMode();
        FtcFlags.init();
    }

    void setDebugMode() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(
                getTextResource("plugin.yml")
        );

        debugMode = config.getBoolean("debug_build");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        AutoSave.get().run();

        runSafe(MobHealthBar::shutdown);
        runSafe(Bosses::shutdown);
        runSafe(WorldLoader::shutdown);
        runSafe(PacketListeners::removeAll);

        FtcDiscord.staffLog(C_SERVER, "FTC shutting down");
    }

    @Override
    public @NonNull String namespace() {
        return NAMESPACE;
    }
}