package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.WarpArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.useables.warps.Warp;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.UserTeleport;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public class CommandWarpEdit extends FtcCommand {
    public CommandWarpEdit(){
        super("warpedit", Crown.inst());

        setPermission(Permissions.WARP_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("name", StringArgumentType.word())
                                .executes(c -> createWarp(
                                        c.getSource(),
                                        c.getArgument("name", String.class),
                                        c.getSource().asPlayer().getLocation()
                                ))

                                .then(argument("location", PositionArgument.position())
                                        .executes(c -> createWarp(
                                                c.getSource(),
                                                c.getArgument("name", String.class),
                                                c.getArgument("location", Position.class).getLocation(c.getSource())
                                        ))
                                )
                        )
                )

                .then(argument("warp", WarpArgument.warp())
                        .suggests((c, b) -> WarpArgument.warp().listSuggestions(c, b, true))

                        .then(literal("paste")
                                .executes(c -> moveDest(
                                        c.getSource(),
                                        get(c),
                                        c.getSource().asPlayer().getLocation()
                                ))

                                .then(argument("dest", PositionArgument.position())
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            return moveDest(source, get(c), c.getArgument("dest", Position.class).getLocation(source));
                                        })
                                )
                        )

                        .then(literal("goto")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Warp warp = get(c);
                                    Announcer.debug(warp);

                                    user.createTeleport(warp::getDestination, true, UserTeleport.Type.WARP).start(true);
                                    return 0;
                                })
                        )

                        .then(CommandInteractable.getInstance().checksArgument(this::get))

                        .then(literal("delete")
                                .executes(c -> {
                                    Warp warp = get(c);
                                    warp.delete();

                                    c.getSource().sendAdmin(Component.text("Deleting warp ").append(warp.displayName()));
                                    return 0;
                                })
                        )
                );
    }

    private int createWarp(CommandSource c, String name, Location location) throws CommandSyntaxException {
        Warp warp = Crown.getWarpManager().register(Keys.parse(name), location);
        c.sendAdmin(Component.text("Creating warp named ").append(warp.displayName()));
        return 0;
    }

    public int moveDest(CommandSource source, Warp warp, Location location){
        warp.setDestination(location);

        source.sendAdmin(Component.text("Moved paste of ").append(warp.displayName()));
        return 0;
    }

    public Warp get(CommandContext<CommandSource> c){
        return WarpArgument.getWarp(c, "warp");
    }
}
