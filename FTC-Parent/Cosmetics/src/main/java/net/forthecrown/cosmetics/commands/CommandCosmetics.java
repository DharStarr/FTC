package net.forthecrown.cosmetics.commands;

import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandCosmetics extends FtcCommand {

    public CommandCosmetics(){
        super("cosmetics", Cosmetics.getPlugin());

        setPermission(Permissions.DEFAULT);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Opens the Cosmetics menu
     *
     *
     * Valid usages of command:
     * - /cosmetics
     *
     * Author: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
           CrownUser u = getUserSender(c);
           u.getPlayer().openInventory(Cosmetics.plugin.getMainCosmeticInventory(u));
           return 0;
        });
    }
}
