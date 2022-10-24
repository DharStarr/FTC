package net.forthecrown.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionProperty;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserHomes;

public class RegionParseResult {
    private final UserParseResult userParse;
    private PopulationRegion region;

    public RegionParseResult(UserParseResult userParse) {
        this.userParse = userParse;
        this.region = null;
    }

    public RegionParseResult(PopulationRegion region) {
        this.region = region;
        this.userParse = null;
    }

    /**
     * Gets a region
     * @param source The source getting the region
     * @param checkInvite Whether this should check if the source was invited to the region
     * @return The parsed region
     * @throws CommandSyntaxException Ifis true, then it will
     * be thrown either by the user not having a home region, or if checkInvite is true, then it will
     * be thrown because the source is not invited to the parsed region
     */
    public PopulationRegion getRegion(CommandSource source, boolean checkInvite) throws CommandSyntaxException {
        PopulationRegion region;

        if (userParse == null) {
            region = this.region;
        } else {
            User user = userParse.getUser(source, checkInvite);
            boolean self = source.textName().equals(user.getName());

            UserHomes homes = user.getHomes();
            var homePos = homes.getHomeRegion();

            if (homePos == null) {
                if (self) {
                    throw Exceptions.NO_HOME_REGION;
                } else {
                    throw Exceptions.noHomeRegion(user);
                }
            }

            region = RegionManager.get()
                    .get(homePos);
        }

        if (!source.isPlayer()
                || !checkInvite
                || source.hasPermission(Permissions.REGIONS_ADMIN)
        ) {
            return region;
        }

        User sourceUser = Users.get(source.asPlayer());
        boolean invited = region.hasValidInvite(sourceUser.getUniqueId());

        if (invited) {
            return region;
        }

        if (region.hasName()) {
            if (region.hasProperty(RegionProperty.PRIVATE_POLE)) {
                throw Exceptions.privateRegion(region);
            }

            return region;
        }

        throw Exceptions.notInvited(
                region.getInviter(sourceUser.getUniqueId())
        );
    }
}