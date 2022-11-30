package net.forthecrown.core.challenge;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.economy.sell.SellShop;
import net.forthecrown.economy.sell.SellShopNodes;
import net.forthecrown.log.DataLogs;
import net.forthecrown.log.LogEntry;
import net.forthecrown.log.LogQuery;
import net.forthecrown.user.User;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.*;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Range;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Consumer;

/** Utility class for challenge-related functions */
public @UtilityClass class Challenges {
    public final String
            METHOD_ON_RESET     = "onReset",
            METHOD_ON_ACTIVATE  = "onActivate",
            METHOD_CAN_COMPLETE = "canComplete",
            METHOD_ON_COMPLETE  = "onComplete",
            METHOD_GET_PLAYER   = "getPlayer",
            METHOD_ON_EVENT     = "onEvent",

            METHOD_STREAK_INCREASE = "onStreakIncrease";

    public void logActivation(Holder<Challenge> challenge, String extra) {
        LogEntry entry = LogEntry.of(ChallengeLogs.ACTIVE)
                .set(ChallengeLogs.A_CHALLENGE, challenge.getKey())
                .set(ChallengeLogs.A_TYPE, challenge.getValue().getResetInterval());

        if (!Strings.isNullOrEmpty(extra)) {
            entry.set(ChallengeLogs.A_EXTRA, extra);
        }

        DataLogs.log(ChallengeLogs.ACTIVE, entry);
    }

    public void logCompletion(Holder<Challenge> challenge, UUID uuid) {
        LogEntry entry = LogEntry.of(ChallengeLogs.COMPLETED)
                .set(ChallengeLogs.C_PLAYER, uuid)
                .set(ChallengeLogs.C_CHALLENGE, challenge.getKey());

        DataLogs.log(ChallengeLogs.COMPLETED, entry);
    }

    public boolean hasCompleted(Challenge challenge, UUID uuid) {
        return ChallengeManager.getInstance()
                .getChallengeRegistry()
                .getHolderByValue(challenge)
                .map(holder -> hasCompleted(holder, uuid))
                .orElse(false);
    }

    public boolean hasCompleted(Holder<Challenge> challenge, UUID uuid) {
        var reset = challenge.getValue()
                .getResetInterval();

        LocalDate start = switch (reset) {
            case DAILY -> LocalDate.now();
            case WEEKLY -> LocalDate.now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MANUAL -> LocalDate.MIN;
        };

        return !DataLogs.query(
                LogQuery.builder(ChallengeLogs.COMPLETED)
                        .maxResults(1)
                        .queryRange(Range.between(start, LocalDate.now()))

                        .field(ChallengeLogs.C_PLAYER)
                        .add(uuid1 -> Objects.equals(uuid1, uuid))

                        .field(ChallengeLogs.C_CHALLENGE)
                        .add(s -> Objects.equals(s, challenge.getKey()))

                        .build()
        ).isEmpty();
    }

    public void trigger(String challengeName, Object input) {
        apply(challengeName, challenge -> challenge.trigger(input));
    }

    public boolean isActive(Challenge challenge) {
        return ChallengeManager.getInstance()
                .getActiveChallenges()
                .contains(challenge);
    }

    public void apply(Challenge challenge,
                      Consumer<Holder<Challenge>> consumer
    ) {
        ChallengeManager.getInstance()
                .getChallengeRegistry()
                .getHolderByValue(challenge)
                .ifPresent(holder -> {
                    if (!isActive(holder.getValue())) {
                        return;
                    }

                    consumer.accept(holder);
                });
    }

    public void apply(String challengeName, Consumer<Challenge> consumer) {
        ChallengeManager.getInstance()
                .getChallengeRegistry()
                .get(challengeName)
                .ifPresent(challenge -> {
                    if (!isActive(challenge)) {
                        return;
                    }

                    consumer.accept(challenge);
                });
    }

    Menu createItemMenu(Registry<Challenge> challenges, SellShop shop) {
        MenuBuilder builder = Menus.builder(Menus.MAX_INV_SIZE - 9)
                .setTitle("Daily Item Challenges")
                .addBorder()

                // < Go back
                .add(Slot.ZERO, SellShopNodes.previousPage(shop))

                // Header
                .add(Slot.of(4), createMenuHeader())

                .add(Slot.of(4, 4),
                        MenuNode.builder()
                                .setItem((user, context) -> {
                                    return ItemStacks.builder(Material.BOOK)
                                            .setName("&eInfo")
                                            .addLore("&7This challenge is reset daily. Complete it to build a streak.")
                                            .addLore("&7The longer your streak, the greater the rewards!")
                                            .addLore("")
                                            .addLore("&7Rewards include:")
                                            .addLore("&7Rhines, Gems, Guild EXP and mob Plushies")
                                            .build();
                                })
                                .build()
                );

        for (var h: challenges.entries()) {
            if (!(h.getValue() instanceof ItemChallenge item)) {
                continue;
            }

            builder.add(
                    item.getMenuSlot(),
                    item.toInvOption()
            );
        }

        return builder
                .build();
    }

    public static MenuNode createMenuHeader() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    var builder = ItemStacks.builder(Material.CLOCK)
                            .setName("&bDaily Item Challenges")
                            .setFlags(ItemFlag.HIDE_ATTRIBUTES);

                    int streak = queryStreak(StreakCategory.ITEMS, user)
                            .orElse(0);

                    builder.addLore(
                            Text.format(
                                    "Current streak: {0, number}",
                                    NamedTextColor.GRAY,
                                    streak
                            )
                    );

                    return builder.build();
                })
                .build();
    }

    public OptionalInt queryStreak(Challenge challenge, User user) {
        if (user == null
                || challenge.getResetInterval() == ResetInterval.MANUAL
        ) {
            return OptionalInt.empty();
        }

        return queryStreak(challenge.getStreakCategory(), user);
    }

    public OptionalInt queryStreak(StreakCategory category, User viewer) {
        LocalDate queryStart = LocalDate.MIN;

        var query = LogQuery.builder(ChallengeLogs.STREAK_SCHEMA)
                .queryRange(Range.between(queryStart, LocalDate.now()))
                .maxResults(ChallengeConfig.maxStreak)

                .field(ChallengeLogs.S_CATEGORY)
                .add(c -> Objects.equals(category, c))

                .field(ChallengeLogs.S_PLAYER)
                .add(uuid -> Objects.equals(viewer.getUniqueId(), uuid))

                .build();

        var results = DataLogs.query(query);

        if (results.isEmpty()) {
            return OptionalInt.empty();
        }

        int streak = 0;
        LocalDate lastDate = LocalDate.now();

        for (int i = results.size() - 1; i >= 0; i--) {
            var entry = results.get(i);

            LocalDate eDate = Time.localTime(entry.getDate())
                    .toLocalDate();

            if (eDate.compareTo(lastDate) != 0
                    && !category.areNeighboring(lastDate, eDate)
            ) {
                break;
            }

            streak++;
            lastDate = eDate;
        }

        return streak == 0
                ? OptionalInt.empty()
                : OptionalInt.of(streak);
    }

    public void logStreak(StreakCategory category, UUID uuid) {
        DataLogs.log(
                ChallengeLogs.STREAK_SCHEMA,

                LogEntry.of(ChallengeLogs.STREAK_SCHEMA)
                        .set(ChallengeLogs.S_CATEGORY, category)
                        .set(ChallengeLogs.S_PLAYER, uuid)
        );
    }
}