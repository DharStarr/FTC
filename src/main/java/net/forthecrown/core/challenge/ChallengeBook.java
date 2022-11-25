package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.economy.TransactionType;
import net.forthecrown.economy.Transactions;
import net.forthecrown.user.User;
import net.forthecrown.utils.book.BookBuilder;
import net.forthecrown.utils.text.TextInfo;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

public class ChallengeBook {
    public static final int PAY_COST = 10_000;

    public static final ClickableTextNode PAY_NODE = ClickableTexts.register(
            new ClickableTextNode("challenge_pay")
                    .setExecutor(user -> {
                        if (!user.hasBalance(PAY_COST)) {
                            throw Exceptions.cannotAfford(PAY_COST);
                        }

                        Challenges.apply("daily/pay", challenge -> {
                            if (!challenge.canComplete(user)) {
                                return;
                            }

                            user.removeBalance(PAY_COST);
                            challenge.trigger(user);

                            user.sendMessage(
                                    Text.format(
                                            "Paid &e{0, rhines}&r to complete challenge!",
                                            NamedTextColor.GRAY,
                                            PAY_COST
                                    )
                            );

                            Transactions.builder()
                                    .type(TransactionType.PAY_CHALLENGE)
                                    .sender(user.getUniqueId())
                                    .amount(PAY_COST)
                                    .log();
                        });

                        open(user);
                    })

                    .setPrompt(user -> {
                        return Text.format("[{0, rhines}]",
                                NamedTextColor.DARK_AQUA,
                                PAY_COST
                        )
                                .hoverEvent(text("Click to pay"));
                    })
    );

    public static void open(User user) {
        BookBuilder builder = new BookBuilder()
                .setAuthor("")
                .setTitle("Challenge progress");

        ChallengeEntry entry = ChallengeManager.getInstance()
                .getOrCreateEntry(user.getUniqueId());

        mainPage(builder, entry);

        for (var r: ResetInterval.values()) {
            challengePage(builder, entry, r);
        }

        user.openBook(builder.build());
    }

    private static void mainPage(BookBuilder builder, ChallengeEntry entry) {
        builder.addCentered(text("Challenges"))
                .addEmptyLine()
                .addText(text("Progress:"));

        // Left: Total
        // Right: Completed
        Map<ResetInterval, IntIntPair>
                summary = new EnumMap<>(ResetInterval.class);

        for (var c: ChallengeManager.getInstance().getActiveChallenges()) {
            boolean completed = Challenges.hasCompleted(c, entry.getId());

            IntIntPair pair = summary.computeIfAbsent(
                    c.getResetInterval(),
                    interval -> new IntIntMutablePair(0, 0)
            );

            pair.left(pair.leftInt() + 1);

            if (completed) {
                pair.right(pair.rightInt() + 1);
            }
        }

        for (var e: summary.entrySet()) {
            int total = e.getValue().leftInt();
            int completed = e.getValue().rightInt();

            if (total <= 0) {
                continue;
            }

            builder.addField(
                    text(e.getKey().getDisplayName() + " Challenges"),

                    Text.format("{0, number}/{1, number}",
                            completed >= total
                                    ? NamedTextColor.DARK_GREEN
                                    : NamedTextColor.GRAY,

                            completed, total
                    )
            );
        }

        builder.newPage();
    }

    private static void challengePage(BookBuilder builder,
                                      ChallengeEntry entry,
                                      ResetInterval interval
    ) {
        List<Challenge> activeList = new ObjectArrayList<>();
        activeList.addAll(
                ChallengeManager.getInstance()
                        .getActiveChallenges()
        );

        activeList.removeIf(challenge -> challenge.getResetInterval() != interval);

        if (activeList.isEmpty()) {
            return;
        }

        Object2BooleanMap<Challenge>
                completed = new Object2BooleanOpenHashMap<>();

        for (var c: activeList) {
            boolean isCompleted = Challenges.hasCompleted(c, entry.getId());
            completed.put(c, isCompleted);
        }

        builder.addCentered(
                text(interval.getDisplayName() + " Challenges")
        );

        float totalProgress = 0.0F;
        float totalRequired = 0.0F;

        for (var e: completed.object2BooleanEntrySet()) {
            var c = e.getKey();
            boolean isCompleted = e.getBooleanValue();

            float progress = entry.getProgress()
                    .getFloat(c);

            ++totalRequired;

            if (isCompleted || progress >= c.getGoal()) {
                ++totalProgress;
            } else {
                totalProgress += (progress / c.getGoal());
            }
        }

        if (totalRequired != 0.0F) {
            double progress = (totalProgress / totalRequired) * 100;

            builder.addCentered(
                    Text.format("{0, number, #.#}% done.",
                            NamedTextColor.GRAY,
                            progress
                    )
            );
        }

        builder.addEmptyLine();
        Challenge payChallenge = ChallengeManager.getInstance()
                .getChallengeRegistry()
                .get("daily/pay")
                .orElse(null);

        for (var e: completed.object2BooleanEntrySet()) {
            var c = e.getKey();
            boolean isCompleted = e.getBooleanValue();

            float progress = entry.getProgress()
                    .getFloat(c);

            if (isCompleted) {
                progress = c.getGoal();
            }

            Component displayName = c.displayName()
                    .color(null);

            if (Objects.equals(payChallenge, c) && !isCompleted) {
                displayName = displayName.append(space())
                        .append(PAY_NODE.prompt(entry.getUser()));
            }

            int displayNameSize = TextInfo.getPxWidth(Text.plain(displayName));
            int filler = BookBuilder.PIXELS_PER_LINE - displayNameSize;

            if (filler > 1) {
                displayName = displayName
                        .append(
                                text(
                                        TextInfo.getFiller(filler)
                                                .replaceAll("`", "."),
                                        NamedTextColor.GRAY
                                )
                        );
            }

            builder
                    .addText(displayName)

                    .justifyRight(
                            Text.format("{0, number, -floor}/{1, number}",
                                    isCompleted
                                            ? NamedTextColor.DARK_GREEN
                                            : NamedTextColor.GRAY,

                                    progress, c.getGoal()
                            )
                                    .hoverEvent(
                                            text(isCompleted
                                                    ? "Completed"
                                                    : "Uncompleted"
                                            )
                                    )
                    );
        }

        builder.newPage();
    }
}