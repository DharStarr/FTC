package net.forthecrown.core.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.script.ScriptResult;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;

@Getter
@AllArgsConstructor
public class ScriptEventListener implements Listener, EventExecutor {
    private static final Logger LOGGER = FTC.getLogger();

    NashornScriptEngine engine;

    final ChallengeHandle handle;

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event
    ) throws EventException {
        if (event instanceof Cancellable cancellable
                && cancellable.isCancelled()
        ) {
            return;
        }

        if (engine != null) {
            var result = ScriptResult.success(engine)
                    .invoke(Challenges.METHOD_ON_EVENT, event, handle);

            if (!result.isMissingMethod()) {
                return;
            }
        }

        Player player;

        if (engine != null) {
            var runResult = ScriptResult.success(engine)
                    .invoke(Challenges.METHOD_GET_PLAYER, event);

            if (runResult.isMissingMethod()) {
                player = getFromEvent(event);
            } else {
                if (!runResult.hasResult()) {
                    LOGGER.error("No result returned by getPlayer in script!");
                    return;
                }

                player = ChallengeHandle.getPlayer(runResult.getResult());
            }
        } else {
            player = getFromEvent(event);
        }

        if (player == null) {
            return;
        }

        handle.givePoint(player);
    }

    private @Nullable Player getFromEvent(Event event) {
        if (event instanceof PlayerEvent event1) {
            return event1.getPlayer();
        }

        if (event instanceof PlayerDeathEvent event1) {
            return event1.getPlayer();
        }

        LOGGER.error(
                "Cannot execute challenge event! No getPlayer " +
                        "method specified in script and event " +
                        "is not a player event!",
                new RuntimeException()
        );

        return null;
    }
}