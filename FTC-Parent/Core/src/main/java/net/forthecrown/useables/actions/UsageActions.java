package net.forthecrown.useables.actions;

import net.forthecrown.core.CrownCore;

import static net.forthecrown.registry.Registries.USAGE_ACTIONS;

public class UsageActions {
    public static void init(){
        register(new ActionChangeScore(ActionChangeScore.Action.DECREMENT));
        register(new ActionChangeScore(ActionChangeScore.Action.INCREMENT));
        register(new ActionChangeScore(ActionChangeScore.Action.SET));

        register(new ActionRemoveNumber(false));
        register(new ActionRemoveNumber(true));

        register(new ActionAddNumber(false));
        register(new ActionAddNumber(true));

        register(new ActionCommand(false));
        register(new ActionCommand(true));

        register(new ActionItem(false));
        register(new ActionItem(true));

        register(new ActionKit());
        register(new ActionShowText());
        register(new ActionTeleport());

        USAGE_ACTIONS.close();
        CrownCore.logger().info("Default actions registered");
    }

    private static void register(UsageAction action){
        USAGE_ACTIONS.register(action.key(), action);
    }
}