package net.forthecrown.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.World;

public class CommandTime extends FtcCommand {

    public CommandTime() {
        super("time");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /time add <amount>
     * /time set <amount>
     * /time query
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("set")

                        .then(timeArg("day", 1000))
                        .then(timeArg("noon", 6000))
                        .then(timeArg("night", 13000))
                        .then(timeArg("midnight", 18000))

                        .then(argument("time", TimeArgument.time())
                                .executes(c -> timeThing(c, false))
                        )
                )

                .then(literal("add")
                        .then(argument("time", TimeArgument.time())
                                .executes(c -> timeThing(c, true))
                        )
                )

                .then(literal("get")
                        .then(argument("world", WorldArgument.world())
                                .executes(c -> {
                                    World world = c.getArgument("world", World.class);
                                    return timeInfo(c.getSource(), world);
                                })
                        )

                        .executes(c -> {
                            World world = c.getSource().getWorld();
                            return timeInfo(c.getSource(), world);
                        })
                );
    }

    private int timeInfo(CommandSource source, World world) {
        source.sendAdmin(
                Component.text("World times:")

                        .append(Component.newline())
                        .append(Component.text("Full time: " + world.getFullTime()))

                        .append(Component.newline())
                        .append(Component.text("Time: " + world.getTime()))

                        .append(Component.newline())
                        .append(Component.text("Day: " + (world.getFullTime()/1000/24)))

                        .append(Component.newline())
                        .append(Component.text("Year: " + FtcUtils.worldTimeToYears(world)))
        );
        return 0;
    }

    private LiteralArgumentBuilder<CommandSource> timeArg(String name, int time) {
        return literal(name).executes(c -> {
            World world = c.getSource().getWorld();
            long timeAdd = world.getFullTime() - (world.getFullTime() % ServerLevel.TICKS_PER_DAY);

            return setTime(c.getSource(), time + timeAdd);
        });
    }

    private int timeThing(CommandContext<CommandSource> c, boolean add) {
        int time = c.getArgument("time", Integer.class);
        World world = c.getSource().getWorld();

        long actualTime = time + (add ? world.getFullTime() : 0);
        return setTime(c.getSource(), actualTime);
    }

    private int setTime(CommandSource source, long time) {
        World world = source.getWorld();

        world.setFullTime(time);

        source.sendAdmin(
                Component.text("Set time of ")
                        .append(Component.text(world.getName()))
                        .append(Component.text(" to " + time))
        );
        return 0;
    }
}