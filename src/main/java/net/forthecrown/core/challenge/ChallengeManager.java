package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.forthecrown.core.AutoSave;
import net.forthecrown.core.DayChange;
import net.forthecrown.core.DayChangeListener;
import net.forthecrown.core.FTC;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.log.DataManager;
import net.forthecrown.log.LogQuery;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.PathUtil;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

public class ChallengeManager implements DayChangeListener {
    private static final Logger LOGGER = FTC.getLogger();

    @Getter
    private static final ChallengeManager instance = new ChallengeManager();

    @Getter
    private LocalDate date;

    private final List<Challenge>
            activeChallenges = new ObjectArrayList<>();

    private final Map<UUID, ChallengeEntry>
            entries = new Object2ObjectOpenHashMap<>();

    @Getter
    private final Registry<Challenge>
            challengeRegistry = Registries.newRegistry();

    @Getter
    private final ChallengeDataStorage storage;

    public ChallengeManager() {
        date = LocalDate.now();

        this.storage = new ChallengeDataStorage(
                PathUtil.getPluginDirectory("challenges")
        );

        storage.ensureDefaultsExist();
    }

    // Called reflectively in BootStrap
    private static void init() {
        instance.registerItemChallenges();
        instance.loadChallenges();
        instance.load();

        DayChange.get().addListener(instance);
        AutoSave.get().addCallback(getInstance()::save);

        ConfigManager.get()
                .registerConfig(ChallengeConfig.class);
    }

    private void registerItemChallenges() {

    }

    public ChallengeEntry getEntry(UUID uuid) {
        return entries.get(uuid);
    }

    public ChallengeEntry getOrCreateEntry(UUID uuid) {
        return entries.computeIfAbsent(uuid, ChallengeEntry::new);
    }

    public Collection<Challenge> getActiveChallenges() {
        return Collections.unmodifiableCollection(activeChallenges);
    }

    @Override
    public void onDayChange(ZonedDateTime time) {
        save();
        date = time.toLocalDate();

        reset(ResetInterval.DAILY);
        if (time.getDayOfWeek() == DayOfWeek.MONDAY) {
            reset(ResetInterval.WEEKLY);
        }
    }

    public void reset(ResetInterval interval) {
        Set<Challenge> current = new ObjectOpenHashSet<>();

        activeChallenges.removeIf(challenge -> {
            if (challenge.getResetInterval() != interval) {
                return false;
            }

            challenge.deactivate();
            current.add(challenge);

            return true;
        });

        entries.values().forEach(entry -> entry.onReset(interval));

        if (!interval.shouldRefill()) {
            return;
        }

        List<Challenge> challenges = challengeRegistry.values()
                .stream()
                .filter(challenge -> challenge.getResetInterval() == interval)
                .collect(ObjectArrayList.toList());

        if (interval.getMax() != -1
                && challenges.size() > interval.getMax()
        ) {
            challenges.removeIf(challenge -> {
                if (ChallengeConfig.allowRepeatingChallenges) {
                    return false;
                }

                return current.contains(challenge);
            });
        }

        if (challenges.isEmpty()) {
            LOGGER.warn("Found no {} challenges to use!", interval);
            return;
        }

        int required = Math.min(
                challenges.size(),
                interval.getMax()
        );

        Set<Challenge> picked = Util.pickUniqueEntries(
                challenges,
                Util.RANDOM,
                required
        );

        picked.forEach(challenge -> activate(challenge, true));

        LOGGER.debug("Reset all {} challenges, added {} new ones",
                interval, picked.size()
        );
    }

    public void activate(Challenge challenge, boolean log) {
        var extra = challenge.activate();
        activeChallenges.add(challenge);

        if (log) {
            Challenges.logActivation(challenge, extra);
        }
    }

    public void clear() {
        activeChallenges.forEach(Challenge::deactivate);
        activeChallenges.clear();

        entries.clear();
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    private void loadChallenges() {
        challengeRegistry.clear();
        challengeRegistry.removeIf(
                holder -> holder.getValue() instanceof JsonChallenge
        );

        storage.loadChallenges(challengeRegistry);
    }

    public void save() {
        storage.saveEntries(entries.values(), challengeRegistry);
    }

    public void load() {
        clear();

        storage.loadEntries(challengeRegistry)
                .resultOrPartial(LOGGER::error)
                .ifPresent(entries1 -> {
                    for (var e: entries1) {
                        entries.put(e.getId(), e);
                    }
                });

        loadActive();
    }

    private void loadActive() {
        var list = DataManager.getInstance()
                .queryLogs(
                        LogQuery.builder(ChallengeLogs.ACTIVE)
                                .build()
                )
                .toList();

        if (list.isEmpty()) {
            reset(ResetInterval.DAILY);
            reset(ResetInterval.WEEKLY);

            return;
        }

        list.forEach(entry -> {
            String key = entry.get(ChallengeLogs.A_CHALLENGE);

            challengeRegistry.get(key)
                    .ifPresentOrElse(
                            challenge -> activate(challenge, false),

                            () -> {
                                LOGGER.warn(
                                        "Unknown challenge found in data logs: '{}'",
                                        key
                                );
                            }
                    );
        });
    }
}