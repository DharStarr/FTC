package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.ComponentType;

public class CommandBroadcast extends CrownCommandBuilder {

    public CommandBroadcast(){
        super("broadcast", FtcCore.getInstance());

        setDescription("Broadcasts a message to the entire server.");
        setAliases("announce", "bc", "ac");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Broadcasts a message to the entire server
     *
     *
     * Valid usages of command:
     * - /broadcast
     * - /bc
     *
     * Permissions used:
     * - ftc.commands.broadcast
     *
     * Author: Wout
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("announcement", StringArgumentType.greedyString())
                        .executes(context -> {
                            FtcCore.getAnnouncer().announce(context.getArgument("announcement", String.class));
                            return 0;
                        })
                )
                .then(argument("-component")
                        .then(argument("componentAnnouncement", ComponentType.component())
                                .executes(c -> {
                                    FtcCore.getAnnouncer().announce(ComponentType.getAdventure(c, "componentAnnouncement"));
                                    return 0;
                                })
                             )
                );
    }
}
