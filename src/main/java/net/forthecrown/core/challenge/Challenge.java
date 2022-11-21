package net.forthecrown.core.challenge;

import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;

public interface Challenge {
    Component displayName();

    ResetInterval getResetInterval();

    float getGoal();

    default boolean canComplete(User user) {
        return true;
    }

    default void onComplete(User user) {

    }

    void deactivate();

    String activate();

}