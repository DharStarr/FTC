package net.forthecrown.events.player;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PlaytimeChallengeListener implements Listener {
    public static final long TIME = 60 * 20;

    private final Map<UUID, TaskRunnable>
            tasks = new Object2ObjectOpenHashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskRunnable runnable = new TaskRunnable(event.getPlayer());
        TaskRunnable old = tasks.put(event.getPlayer().getUniqueId(), runnable);

        if (old != null) {
            old.cancelled = true;
        }

        Tasks.runTimer(runnable, TIME, TIME);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var runnable = tasks.remove(event.getPlayer().getUniqueId());

        if (runnable != null) {
            runnable.cancelled = true;
        }
    }

    @RequiredArgsConstructor
    private class TaskRunnable implements Consumer<BukkitTask> {
        private final Player player;
        private int minutesPassed = 0;
        private boolean cancelled;

        @Override
        public void accept(BukkitTask task) {
            if (cancelled) {
                Tasks.cancel(task);
                return;
            }

            ++minutesPassed;

            var challenge = ChallengeManager.getInstance()
                    .getChallengeRegistry()
                    .get("daily/playtime")
                    .orElseThrow();

            challenge.trigger(player);

            if (minutesPassed >= challenge.getGoal()) {
                tasks.remove(player.getUniqueId());
                Tasks.cancel(task);
            }
        }
    }
}