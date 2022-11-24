package net.forthecrown.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.challenge.Challenge;
import net.forthecrown.core.challenge.ChallengeBook;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.challenge.Challenges;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.Predicate;

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

    private static final Predicate<CommandSource> IS_ADMIN = source -> {
        return source.hasPermission(Permissions.CHALLENGES_ADMIN);
    };

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    var user = getUserSender(c);
                    ChallengeBook.open(user);

                    return 0;
                })

                .then(literal("list")
                        .requires(IS_ADMIN)

                        .executes(c -> {
                            var it = ChallengeManager.getInstance()
                                    .getChallengeRegistry()
                                    .entries()
                                    .iterator();

                            TextWriter writer = TextWriters.newWriter();

                            while (it.hasNext()) {
                                var next = it.next();

                                writer.formattedLine(
                                        "key={0} id={1}, name={2}",
                                        next.getKey(),
                                        next.getId(),
                                        next.getValue().displayName()
                                );
                            }

                            c.getSource().sendMessage(writer);
                            return 0;
                        })
                )

                .then(literal("list_active")
                        .requires(IS_ADMIN)

                        .executes(c -> {
                            var it = ChallengeManager.getInstance()
                                    .getActiveChallenges()
                                    .listIterator();

                            TextWriter writer = TextWriters.newWriter();

                            while (it.hasNext()) {
                                var next = it.next();

                                writer.formattedLine(
                                        "{0}) {1}",
                                        it.nextIndex(),
                                        next.displayName()
                                );
                            }

                            c.getSource().sendMessage(writer);
                            return 0;
                        })
                )

                .then(literal("give_points")
                        .requires(IS_ADMIN)

                        .then(argument("challenge", RegistryArguments.CHALLENGE)
                                .then(argument("user", Arguments.ONLINE_USER)
                                        .executes(c -> givePoints(c, 1))

                                        .then(argument("points", FloatArgumentType.floatArg(1))
                                                .executes(c -> {
                                                    float points = c.getArgument("points", Float.class);
                                                    return givePoints(c, points);
                                                })
                                        )
                                )
                        )
                )

                .then(literal("trigger")
                        .requires(IS_ADMIN)

                        .then(argument("challenge", RegistryArguments.CHALLENGE)
                                .then(argument("user", Arguments.ONLINE_USER)
                                        .executes(this::trigger)
                                )
                        )
                );
    }

    private int trigger(CommandContext<CommandSource> c)
            throws CommandSyntaxException
    {
        User user = Arguments.getUser(c, "user");
        Holder<Challenge> holder = c.getArgument("challenge", Holder.class);

        if (!Challenges.isActive(holder.getValue())) {
            throw Exceptions.nonActiveChallenge(holder.getValue());
        }

        holder.getValue()
                .trigger(user.getPlayer());

        c.getSource().sendAdmin(
                Text.format("Invoking {0} for {1, user}",
                        holder.getValue().displayName(),
                        user
                )
        );
        return 0;
    }

    private int givePoints(CommandContext<CommandSource> c,
                           float points
    ) throws CommandSyntaxException {
        User user = Arguments.getUser(c, "user");
        Holder<Challenge> holder = c.getArgument("challenge", Holder.class);

        if (!Challenges.isActive(holder.getValue())) {
            throw Exceptions.nonActiveChallenge(holder.getValue());
        }

        ChallengeManager.getInstance()
                .getOrCreateEntry(user.getUniqueId())
                .addProgress(holder.getValue(), points);

        c.getSource().sendAdmin(
                Text.format("Gave &e{0, user} &6{1, number}&r points for &f{2}&r.",
                        NamedTextColor.GRAY,
                        user,
                        points,
                        holder.getValue().displayName()
                )
        );
        return 0;
    }
}