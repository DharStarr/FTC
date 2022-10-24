package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

import java.util.ArrayList;
import java.util.Collection;

public class CommandInvite extends FtcCommand {

    public CommandInvite() {
        super("invite");

        setPermission(Permissions.REGIONS);
        setDescription("Invites players to your region");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Invites users to your region
     *
     * Valid usages of command:
     * /Invite
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("users", Arguments.USERS)
                        .executes(c -> {
                            User user = getUserSender(c);
                            RegionPos homePos = user.getHomes().getHomeRegion();

                            if (homePos == null) {
                                throw Exceptions.NO_HOME_REGION;
                            }

                            PopulationRegion region = RegionManager.get()
                                    .get(homePos);

                            Collection<User> users = new ArrayList<>(Arguments.getUsers(c, "users"));

                            users.removeIf(t -> {
                                if (!t.isOnline()
                                        || region.hasValidInvite(t.getUniqueId())
                                ) {
                                    return true;
                                }

                                return Users.areBlocked(user, t);
                            });

                            boolean selfRemoved = users.remove(user);
                            if (users.isEmpty()) {
                                if (selfRemoved) {
                                    throw Exceptions.CANNOT_INVITE_SELF;
                                }

                                throw Exceptions.NO_USERS_FOUND;
                            }

                            byte inviteCount = 0;
                            for (User target: users) {
                                inviteCount++;

                                region.invite(user.getUniqueId(), target.getUniqueId());

                                user.sendMessage(Messages.senderInvited(target));
                                target.sendMessage(Messages.targetInvited(user));
                            }

                            if (inviteCount > 1) {
                                user.sendMessage(Messages.invitedTotal(inviteCount));
                            }

                            return 0;
                        })
                );
    }
}