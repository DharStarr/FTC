package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.RegionVisit;
import net.forthecrown.user.actions.UserActionHandler;

public class CommandHomePole extends FtcCommand {

    public CommandHomePole() {
        super("homepole");

        setAliases("homepost", "homeregion");
        setPermission(Permissions.REGIONS);
        setDescription("Takes you to your home region");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Takes you to your home pole, if you have one
     *
     * Valid usages of command:
     * /HomePole
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    RegionUtil.validateWorld(user.getWorld());

                    RegionPos cords = user.getHomes().getHomeRegion();
                    if(cords == null) throw FtcExceptionProvider.translatable("regions.noHome");

                    PopulationRegion region = Crown.getRegionManager().get(cords);

                    RegionUtil.validateDistance(region.getPolePosition(), user);

                    RegionVisit action = new RegionVisit(user, region);
                    UserActionHandler.handleAction(action);

                    return 0;
                });
    }
}