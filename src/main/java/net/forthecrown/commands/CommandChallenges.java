package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.challenge.ChallengeBook;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandChallenges extends FtcCommand {

    public CommandChallenges() {
        super("Challenges");

        setPermission(Permissions.CHALLENGES);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Challenges
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    var user = getUserSender(c);
                    ChallengeBook.open(user);

                    return 0;
                });
    }
}