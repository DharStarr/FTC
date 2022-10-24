package net.forthecrown.core;

import lombok.Getter;
import net.forthecrown.utils.text.format.PeriodFormat;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import org.apache.logging.log4j.Logger;
import org.bukkit.scheduler.BukkitTask;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * DayUpdate listens to a change in the day.
 * <p>
 * <b>Word of warning</b>: Don't use the {@link Calendar} in the future,
 * use {@link java.time.ZonedDateTime}, it's more up to date and
 * easier to use and understand
 * </p>
 */
public class DayChange {
    /* ----------------------------- CONSTANTS ------------------------------ */

    private static final Logger LOGGER = FTC.getLogger();

    private static final DayChange INSTANCE = new DayChange();

    public static final TemporalAdjuster NEXT_DAY = temporal -> {
        return temporal.plus(1, DAYS)
                .with(HOUR_OF_DAY, 0)
                .with(MINUTE_OF_HOUR, 0)
                .with(SECOND_OF_MINUTE, 0)
                .with(MILLI_OF_SECOND, 1);
    };

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    @Getter
    private final List<DayChangeListener> listeners = new ArrayList<>();

    private BukkitTask updateTask;

    public static DayChange get() {
        return INSTANCE;
    }

    private void changeDay() {
        LOGGER.info("Updating date");

        ZonedDateTime time = ZonedDateTime.now();
        listeners.forEach(r -> {
            try {
                r.onDayChange(time);
            } catch (Throwable e){
                LOGGER.error("Could not update date of " + r.getClass().getSimpleName(), e);
            }
        });
    }

    void schedule() {
        // Cancel if previous task exists
        updateTask = Tasks.cancel(updateTask);
        // Find difference between now and tomorrow
        long difference = Time.timeUntil(getNextDayChange());

        LOGGER.info("DayUpdate scheduled, executing in: {}", PeriodFormat.of(difference));

        // Convert to ticks for bukkit scheduler
        difference = Time.millisToTicks(difference);

        // Run update on next day change and then run it every
        // 24 hours, aka once a day. It probably won't get ran
        // a second time cuz of daily restart, but whatever lol
        // future-proof :D
        updateTask = Tasks.runTimer(
                this::changeDay,
                difference,
                Time.millisToTicks(TimeUnit.DAYS.toMillis(1))
        );
    }

    public static long getNextDayChange() {
        // Configure calendar to be at the start of the next day
        // So we can run day update exactly on time. As always,
        // there's probably a better way of doing this, but IDK lol
        ZonedDateTime time = ZonedDateTime.now();
        time = time.with(NEXT_DAY);

        return Time.toTimestamp(time);
    }

    public void addListener(DayChangeListener runnable) {
        listeners.add(Objects.requireNonNull(runnable));
    }
}