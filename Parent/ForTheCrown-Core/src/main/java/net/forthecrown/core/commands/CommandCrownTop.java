package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.utils.CrownUtils;

public class CommandCrownTop extends CrownCommandBuilder {

    public CommandCrownTop(){
        super("crowntop", FtcCore.getInstance());

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUtils.showLeaderboard(getPlayerSender(c), "crown");
            return 0;
        });
    }
}