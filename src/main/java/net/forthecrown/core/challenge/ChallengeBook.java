package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.user.User;
import net.forthecrown.utils.book.BookBuilder;
import net.forthecrown.utils.book.TextInfo;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class ChallengeBook {
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
        Map<ResetInterval, IntIntPair> summary = new HashMap<>();

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
        List<Challenge> active = new ObjectArrayList<>();
        active.addAll(
                ChallengeManager.getInstance()
                        .getActiveChallenges()
        );

        active.removeIf(challenge -> challenge.getResetInterval() != interval);

        if (active.isEmpty()) {
            return;
        }

        builder.addCentered(
                text(interval.getDisplayName() + " Challenges")
        );

        float totalProgress = 0.0F;
        float totalRequired = 0.0F;

        for (var c: active) {
            totalRequired += c.getGoal();

            float progress = entry.getProgress().getFloat(c);
            totalProgress += progress;
        }

        if (totalRequired != 0.0F && totalProgress != 0.0F) {
            double progress = (totalProgress / totalRequired) * 100;

            builder.addCentered(
                    Text.format("{0, number}% done.",
                            NamedTextColor.GRAY,
                            progress
                    )
            );
        }

        builder.addEmptyLine();

        for (var c: active) {
            float progress = entry.getProgress()
                    .getFloat(c);

            boolean completed = Challenges.hasCompleted(c, entry.getId());

            if (completed) {
                progress = c.getGoal();
            }

            Component displayName = c.displayName()
                    .color(null);

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

            if (!builder.canAddLines(3)) {
                builder.newPage();
            }

            builder
                    .addText(displayName)

                    .justifyRight(
                            Text.format("{0, number}/{1, number}",
                                    completed
                                            ? NamedTextColor.DARK_GREEN
                                            : NamedTextColor.GRAY,

                                    progress, c.getGoal()
                            )
                                    .hoverEvent(
                                            text(completed
                                                    ? "Completed"
                                                    : "Uncompleted"
                                            )
                                    )
                    )
                    .addEmptyLine();
        }

        builder.newPage();
    }
}