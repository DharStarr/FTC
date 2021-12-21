package net.forthecrown.core;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Class which saves the FTC-Core in the interval given in the core_autoSaveIntervalMins comvar
 */
final class PeriodicalSaver extends BukkitRunnable {
    private final Crown core;

    PeriodicalSaver(Crown core) {
        this.core = core;
    }

    public void start() {
        final long inter = ComVars.autoSaveIntervalMins.getValue(60L) * 60 * 20;

        runTaskTimerAsynchronously(core, inter, inter);
    }

    @Override
    public void run() {
        Crown.saveFTC();
    }
}