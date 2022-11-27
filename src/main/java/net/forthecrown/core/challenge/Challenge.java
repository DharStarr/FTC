package net.forthecrown.core.challenge;

import com.google.common.collect.ImmutableList;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

/**
 * Generic interface for challenges
 */
public interface Challenge {
    Component getName();

    default ImmutableList<Component> getDescription() {
        return ImmutableList.of();
    }

    default Component displayName(@Nullable User viewer) {
        TextWriter writer = TextWriters.newWriter();

        for (Component component : getDescription()) {
            writer.line(component);
        }

        int streak = Challenges.queryStreak(this, viewer)
                .orElse(0);

        var reward = getReward();
        if (!reward.isEmpty(viewer, streak)) {
            writer.newLine();
            writer.newLine();

            writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
            writer.setFieldValueStyle(Style.style(NamedTextColor.GRAY));

            reward.write(writer, viewer, streak);
        }

        return getName()
                .color(NamedTextColor.YELLOW)
                .hoverEvent(writer.asComponent());
    }

    default Reward getReward() {
        return Reward.EMPTY;
    }


    default ResetInterval getResetInterval() {
        return ResetInterval.DAILY;
    }

    float getGoal();

    default boolean canComplete(User user) {
        return true;
    }

    default void onComplete(User user) {
        int streak = Challenges.queryStreak(this, user)
                .orElse(0);

        if (getReward().isEmpty(user, streak)) {
            return;
        }

        getReward().give(user, streak);
    }

    void deactivate();

    String activate(boolean reset);

    void trigger(Object input);
}