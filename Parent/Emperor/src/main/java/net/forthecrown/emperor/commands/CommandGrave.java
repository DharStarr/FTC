package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.Grave;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandGrave extends FtcCommand {

    public CommandGrave(){
        super("grave", CrownCore.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("Gives you the items in your grave");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            Grave grave = user.getGrave();

            if(grave.isEmpty()) throw FtcExceptionProvider.emptyGrave();
            grave.giveItems();
            return 0;
        });
    }
}
