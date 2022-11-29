package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Messages;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ChallengeEntry {
    private final UUID id;

    private final Object2FloatMap<Challenge>
            progress = new Object2FloatOpenHashMap<>();

    /* ------------------------------ METHODS ------------------------------- */

    public User getUser() {
        return Users.get(id);
    }

    public void onReset(ResetInterval interval) {
        progress.object2FloatEntrySet().removeIf(entry -> {
            return entry.getKey()
                    .getResetInterval() == interval;
        });

        User user = getUser();
        Component message = Messages.challengesReset(interval);

        if (message != null) {
            user.sendMessage(message);
        }
    }

    public void addProgress(Holder<Challenge> holder, float value) {
        var challenge = holder.getValue();
        float current = progress.getFloat(challenge);

        if (Challenges.hasCompleted(holder, id)) {
            return;
        }

        User user = getUser();

        float newVal = Math.min(
                value + current,
                challenge.getGoal(user)
        );

        if (newVal >= challenge.getGoal(user)) {

            if (!challenge.canComplete(user)) {
                return;
            }

            user.sendMessage(
                    Messages.challengeCompleted(challenge, user)
            );

            Challenges.logCompletion(holder, id);
            challenge.onComplete(user);

            potentiallyAddStreak(challenge.getStreakCategory());
        }

        progress.put(challenge, newVal);
    }

    private void potentiallyAddStreak(StreakCategory category) {
        var manager = ChallengeManager.getInstance();

        for (var c: manager.getActiveChallenges()) {
            if (c.getStreakCategory() != category) {
                continue;
            }

            if (!Challenges.hasCompleted(c, getId())) {
                return;
            }
        }

        getUser().sendMessage(
                Messages.challengeCategoryFinished(category)
        );

        Challenges.logStreak(category, id);
    }
}