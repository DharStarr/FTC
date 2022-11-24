package net.forthecrown.core.challenge;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.log.DataLogs;
import net.forthecrown.log.LogEntry;
import net.forthecrown.log.LogQuery;
import org.apache.commons.lang3.Range;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public @UtilityClass class Challenges {
    public final String
            METHOD_ON_RESET     = "onReset",
            METHOD_ON_ACTIVATE  = "onActivate",
            METHOD_CAN_COMPLETE = "canComplete",
            METHOD_ON_COMPLETE  = "onComplete",
            METHOD_GET_PLAYER   = "getPlayer",
            METHOD_ON_EVENT     = "onEvent";

    public void logActivation(Challenge challenge, String extra) {
        ChallengeManager.getInstance()
                .getChallengeRegistry()
                .getHolderByValue(challenge)

                .ifPresentOrElse(holder -> {
                    LogEntry entry = LogEntry.of(ChallengeLogs.ACTIVE)
                            .set(ChallengeLogs.A_CHALLENGE, holder.getKey())
                            .set(ChallengeLogs.A_TYPE, challenge.getResetInterval())
                            .set(ChallengeLogs.A_TIME, System.currentTimeMillis());

                    if (!Strings.isNullOrEmpty(extra)) {
                        entry.set(ChallengeLogs.A_EXTRA, extra);
                    }

                    DataLogs.log(ChallengeLogs.ACTIVE, entry);
                }, () -> {
                    FTC.getLogger().warn("Unregistered challenge set active!");
                });
    }

    public void logCompletion(Holder<Challenge> challenge, UUID uuid) {
        LogEntry entry = LogEntry.of(ChallengeLogs.COMPLETED)
                .set(ChallengeLogs.TIME, System.currentTimeMillis())
                .set(ChallengeLogs.PLAYER, uuid)
                .set(ChallengeLogs.COMPLETED_CHALLENGE, challenge.getKey());

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

        return DataLogs.query(
                LogQuery.builder(ChallengeLogs.COMPLETED)
                        .maxResults(1)
                        .queryRange(Range.between(start, LocalDate.now()))

                        .field(ChallengeLogs.PLAYER)
                        .add(uuid1 -> Objects.equals(uuid1, uuid))

                        .field(ChallengeLogs.COMPLETED_CHALLENGE)
                        .add(s -> Objects.equals(s, challenge.getKey()))

                        .build()
        )
                .findAny()
                .isPresent();
    }

    public void givePoints(String challengeName, UUID uuid, float points) {
        apply(challengeName, challenge -> {
            ChallengeManager.getInstance()
                    .getOrCreateEntry(uuid)
                    .addProgress(challenge, points);
        });
    }

    public void trigger(String challengeName, Object input) {
        apply(challengeName, challenge -> challenge.trigger(input));
    }

    public boolean isActive(String challengeName) {
        return ChallengeManager.getInstance()
                .getChallengeRegistry()
                .get(challengeName)
                .map(Challenges::isActive)
                .orElse(false);
    }

    public boolean isActive(Challenge challenge) {
        return ChallengeManager.getInstance()
                .getActiveChallenges()
                .contains(challenge);
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
}