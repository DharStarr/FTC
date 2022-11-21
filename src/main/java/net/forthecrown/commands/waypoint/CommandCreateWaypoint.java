package net.forthecrown.commands.waypoint;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserHomes;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoint.*;
import net.forthecrown.waypoint.type.PlayerWaypointType;
import net.forthecrown.waypoint.type.WaypointType;
import net.forthecrown.waypoint.type.WaypointTypes;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;
import java.util.Optional;

public class CommandCreateWaypoint extends FtcCommand {

    public CommandCreateWaypoint() {
        super("CreateWaypoint");

        setPermission(Permissions.WAYPOINTS);
        setDescription("Creates a new waypoint");
        setAliases("waypointcreate");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /CreateWaypoint
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();
                    Block b = player.getTargetBlockExact(
                            5, FluidCollisionMode.NEVER
                    );

                    if (b == null) {
                        throw Exceptions.FACE_WAYPOINT_TOP;
                    }

                    if (WaypointConfig.isDisabledWorld(b.getWorld())) {
                        throw Exceptions.WAYPOINTS_WRONG_WORLD;
                    }

                    PlayerWaypointType type;
                    Vector3i pos = Vectors.from(b);

                    // If attempting to set guild waypoint
                    if (b.getType() == Material.LODESTONE) {
                        type = WaypointTypes.GUILD;

                        User user = Users.get(player);
                        Guild guild = user.getGuild();

                        // Can't make a waypoint for a guild, if you're
                        // not in a guild lol
                        if (guild == null) {
                            throw Exceptions.NOT_IN_GUILD;
                        }

                        // Ensure member has relocation permission
                        GuildMember member = guild.getMember(user.getUniqueId());
                        if (!member.hasPermission(GuildPermission.CAN_RELOCATE)) {
                            throw Exceptions.G_NO_PERM_WAYPOINT;
                        }

                        // Ensure there isn't already a waypoint
                        if (guild.getSettings().getWaypoint() != null) {
                            throw Exceptions.G_WAYPOINT_ALREADY_EXISTS;
                        }

                        // Ensure chunk is owned by the user's guild
                        Guild chunkOwner = GuildManager.get()
                                .getOwner(Vectors.getChunk(pos));

                        if (!Objects.equals(guild, chunkOwner)) {
                            throw Exceptions.G_EXTERNAL_WAYPOINT;
                        }
                    } else if (b.getType() == Material.CHISELED_STONE_BRICKS) {
                        type = WaypointTypes.PLAYER;
                    } else {
                        throw Exceptions.invalidWaypointTop(b.getType());
                    }

                    pos = pos.sub(0, type.getColumn().length - 1, 0);

                    // Ensure the area is correct and validate the
                    // center block column to ensure it's a proper waypoint
                    Optional<CommandSyntaxException>
                            error = Waypoints.isValidWaypointArea(pos, type, b.getWorld(), true);

                    if (error.isPresent()) {
                        throw error.get();
                    }

                    Waypoint waypoint = makeWaypoint(type, pos, c);

                    if (type == WaypointTypes.GUILD) {
                        var user = Users.get(player);
                        var guild = user.getGuild();

                        guild.sendMessage(
                                Messages.guildSetCenter(pos, user)
                        );

                        guild.getSettings()
                                .setWaypoint(waypoint.getId());

                        waypoint.set(WaypointProperties.GUILD_OWNER, guild.getId());
                    } else {
                        waypoint.set(
                                WaypointProperties.OWNER,
                                player.getUniqueId()
                        );

                        User user = Users.get(player);
                        UserHomes homes = user.getHomes();

                        var oldHome = homes.getHomeTeleport();

                        if (oldHome != null) {
                            oldHome.removeResident(user.getUniqueId());

                            if (oldHome.getResidents().isEmpty()) {
                                WaypointManager.getInstance()
                                        .removeWaypoint(waypoint);
                            }
                        }

                        homes.setHomeWaypoint(waypoint);
                    }

                    return 0;
                })

                .then(literal("-admin")
                        .requires(source -> source.hasPermission(Permissions.WAYPOINTS_ADMIN))
                        .executes(c -> {
                            makeWaypoint(WaypointTypes.ADMIN, null, c);
                            return 0;
                        })
                )

                .then(literal("-region_pole")
                        .requires(source -> source.hasPermission(Permissions.WAYPOINTS_ADMIN))

                        .executes(c -> {
                            makeWaypoint(WaypointTypes.REGION_POLE, null, c);
                            return 0;
                        })
                );
    }

    private Waypoint makeWaypoint(WaypointType type,
                             @Nullable Vector3i pos,
                             CommandContext<CommandSource> c
    ) throws CommandSyntaxException {
        Vector3i position;

        if (pos == null) {
            position = Vectors.fromI(c.getSource().getLocation());
        } else {
            position = pos;
        }

        Waypoint waypoint = new Waypoint();
        waypoint.setType(type);
        waypoint.setPosition(position, c.getSource().getWorld());

        if (pos != null) {
            c.getSource().sendMessage(
                    Messages.createdWaypoint(position, type)
            );
        } else {
            c.getSource().sendAdmin(
                    Messages.createdWaypoint(position, type)
            );
        }

        WaypointManager.getInstance()
                .addWaypoint(waypoint);

        return waypoint;
    }
}