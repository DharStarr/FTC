package net.forthecrown.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

/**
 * Class which saves the FTC-Core in the interval given in the autoSaveIntervalMins comvar
 */
public final class AutoSave {
    private static final AutoSave INSTANCE = new AutoSave();

    private BukkitTask task;
    private final Map<Class, Runnable> callbacks = new Object2ObjectOpenHashMap<>();

    public static AutoSave get() {
        return INSTANCE;
    }

    public void schedule() {
        cancel();

        long interval = GeneralConfig.autoSaveInterval;
        interval = Time.millisToTicks(interval);

        task = Tasks.runTimer(this::run, interval, interval);
    }

    public void run() {
        int saved = 0;

        for (var e: callbacks.entrySet()) {
            try {
                e.getValue().run();
                ++saved;
            } catch (Throwable t) {
                FTC.getLogger().error("Couldn't save {}", e.getKey().getSimpleName(), t);
            }
        }

        FTC.getLogger().info("Autosaved {} modules", saved);
    }

    public void cancel() {
        task = Tasks.cancel(task);
    }

    public void addCallback(Class c, Runnable runnable) {
        callbacks.put(c, runnable);
    }

    public void addCallback(Runnable runnable) {
        addCallback(StackLocatorUtil.getCallerClass(2), runnable);
    }
}