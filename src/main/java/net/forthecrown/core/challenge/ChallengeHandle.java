package net.forthecrown.core.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ChallengeHandle {
    private final JsonChallenge challenge;

    public void givePoint(Object playerObject) {
        givePoints(playerObject, 1);
    }

    public void givePoints(Object playerObject, double score) {
        if (hasCompleted(playerObject)) {
            return;
        }

        var player = getPlayer(playerObject);

        ChallengeManager.getInstance()
                .getOrCreateEntry(player.getUniqueId())
                .addProgress(challenge, (float) score);
    }

    public boolean hasCompleted(Object playerObject) {
        var player = getPlayer(playerObject);

        var opt = ChallengeManager.getInstance()
                .getChallengeRegistry()
                .getHolderByValue(challenge);

        if (opt.isEmpty()) {
            return false;
        }

        return Challenges.hasCompleted(opt.get(), player.getUniqueId());
    }

    static Player getPlayer(Object arg) {
        if (arg instanceof Player player) {
            return player;
        }

        if (arg instanceof UUID uuid) {
            return Objects.requireNonNull(
                    Bukkit.getPlayer(uuid),
                    "Unknown player: " + uuid
            );
        }

        if (arg instanceof String string) {
            return Objects.requireNonNull(
                    Bukkit.getPlayerExact(string),
                    "Unknown player: " + string
            );
        }

        if (arg instanceof User user) {
            user.ensureOnline();
            return user.getPlayer();
        }

        if (arg instanceof CommandSource source) {
            return getPlayer(source.asBukkit());
        }

        throw Util.newException("Expected '%s', found '%s'",
                Player.class.getName(),
                arg == null ? null : arg.getClass().getName()
        );
    }
}