package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.EndOpener;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.minecraft.world.level.Level;

public class CommandEndOpener extends FtcCommand {

    public CommandEndOpener() {
        super("EndOpener");

        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Modifies the end opener/closer
     *
     * Valid usages of command:
     * /EndOpener
     *
     * Permissions used:
     * ftc.admin
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        if(Crown.inDebugMode()) {
            command
                    .then(literal("regen")
                            .executes(c -> {
                                c.getSource().sendAdmin("Starting remake");

                                EndOpener opener = opener();
                                opener.regen();

                                c.getSource().sendAdmin("Remaking The End async");
                                return 0;
                            })
                    );
        }

        command
                .then(literal("close")
                        .executes(c -> setOpen(c, false))
                )
                .then(literal("open")
                        .executes(c -> setOpen(c, true))
                )

                .then(accessorArg(OpenerAccessor.CLOSE_MESSAGE))
                .then(accessorArg(OpenerAccessor.OPEN_MESSAGE))
                .then(accessorArg(OpenerAccessor.LEVER_POS))
                .then(accessorArg(OpenerAccessor.ENABLED))
                .then(accessorArg(OpenerAccessor.SIZE));
    }

    int setOpen(CommandContext<CommandSource> c, boolean open) {
        EndOpener opener = opener();
        opener.setOpen(open);

        c.getSource().sendAdmin((open ? "Opened" : "Closed") + " The End");
        return 0;
    }

    private <T> LiteralArgumentBuilder<CommandSource> accessorArg(OpenerAccessor<T> p) {
        return literal(p.getName())
                .executes(c -> {
                    EndOpener opener = opener();

                    Component display = Component.text("End opener " + p.getName() + ": ")
                                    .append(p.display(opener));

                    c.getSource().sendMessage(display);
                    return 0;
                })

                .then(argument("val", p.getType())
                        .executes(c -> {
                            T val = c.getArgument("val", p.getTypeClass());
                            EndOpener opener = opener();
                            p.set(opener, val, c);

                            c.getSource().sendAdmin(
                                    Component.text("Set end opener " + p.getName() + " to ")
                                            .append(p.display(opener))
                            );
                            return 0;
                        })
                );
    }

    private EndOpener opener() {
        return Crown.getEndOpener();
    }

    private interface OpenerAccessor<T> {
        OpenerAccessor<Integer> SIZE = new OpenerAccessor<Integer>() {
            @Override
            public String getName() {
                return "size";
            }

            @Override
            public ArgumentType<Integer> getType() {
                return IntegerArgumentType.integer(1, Level.MAX_LEVEL_SIZE - 12);
            }

            @Override
            public Class<Integer> getTypeClass() {
                return Integer.class;
            }

            @Override
            public Component display(EndOpener opener) {
                return Component.text(opener.getEndSize());
            }

            @Override
            public void set(EndOpener opener, Integer val, CommandContext<CommandSource> c) {
                opener.setEndSize(val);
            }
        };

        OpenerAccessor<Boolean> ENABLED = new OpenerAccessor<>() {
            @Override
            public String getName() {
                return "enabled";
            }

            @Override
            public ArgumentType<Boolean> getType() {
                return BoolArgumentType.bool();
            }

            @Override
            public Class<Boolean> getTypeClass() {
                return Boolean.class;
            }

            @Override
            public Component display(EndOpener opener) {
                return Component.text(opener.isEnabled());
            }

            @Override
            public void set(EndOpener opener, Boolean val, CommandContext<CommandSource> c) {
                opener.setEnabled(val);
            }
        };

        OpenerAccessor<Position> LEVER_POS = new OpenerAccessor<>() {
            @Override
            public String getName() {
                return "leverPos";
            }

            @Override
            public ArgumentType<Position> getType() {
                return PositionArgument.position();
            }

            @Override
            public Class<Position> getTypeClass() {
                return Position.class;
            }

            @Override
            public Component display(EndOpener opener) {
                return FtcFormatter.clickableLocationMessage(opener.getLeverPos().toLocation(), false);
            }

            @Override
            public void set(EndOpener opener, Position val, CommandContext<CommandSource> c) {
                WorldVec3i vec = WorldVec3i.of(val.getLocation(c.getSource()));
                opener.setLeverPos(vec);
            }
        };

        OpenerAccessor<Component> CLOSE_MESSAGE = new ComponentAccessor() {
            @Override
            public String getName() {
                return "closeMessage";
            }

            @Override
            public Component display(EndOpener opener) {
                return opener.getCloseMessage();
            }

            @Override
            public void set(EndOpener opener, Component val, CommandContext<CommandSource> c) {
                opener.setCloseMessage(val);
            }
        };

        OpenerAccessor<Component> OPEN_MESSAGE = new ComponentAccessor() {
            @Override
            public String getName() {
                return "openMessage";
            }

            @Override
            public Component display(EndOpener opener) {
                return opener.getOpenMessage();
            }

            @Override
            public void set(EndOpener opener, Component val, CommandContext<CommandSource> c) {
                opener.setOpenMessage(val);
            }
        };

        String getName();
        ArgumentType<T> getType();
        Class<T> getTypeClass();

        Component display(EndOpener opener);
        void set(EndOpener opener, T val, CommandContext<CommandSource> c);
    }

    interface ComponentAccessor extends OpenerAccessor<Component> {
        @Override
        default ArgumentType<Component> getType() {
            return ChatArgument.chat();
        }

        @Override
        default Class<Component> getTypeClass() {
            return Component.class;
        }
    }
}