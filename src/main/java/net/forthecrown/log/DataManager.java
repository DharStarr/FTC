package net.forthecrown.log;

import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.utils.io.PathUtil;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Getter
public class DataManager {
    @Getter
    private static final DataManager instance = new DataManager();

    private static final Logger LOGGER = FTC.getLogger();

    /* -------------------------- INSTANCE FIELDS --------------------------- */

    private LocalDate date = LocalDate.now();

    private LogContainer logs = new LogContainer();

    private final DataStorage storage;

    private Range<ChronoLocalDate> logRange;

    /* ---------------------------- CONSTRUCTOR ----------------------------- */

    private DataManager() {
        storage = new DataStorage(PathUtil.getPluginDirectory("data"));
        logRange = Range.is(date);
    }

    /* ------------------------------ METHODS ------------------------------- */

    @OnDayChange
    void onDayChange(ZonedDateTime time) {
        save();

        date = time.toLocalDate();
        logs = new LogContainer();

        logRange = Range.between(logRange.getMinimum(), date);
    }

    public Stream<LogEntry> queryLogs(LogQuery query) {
        Stream.Builder<LogEntry> builder = Stream.builder();

        // Used for tracking the amount of found entries
        // to not look for more than query.getMaxResults() results.
        //
        // An integer pointer would be very
        // great for this but no :(
        AtomicInteger found = new AtomicInteger(0);

        // Clamp query's search range to existing logs to
        // prevent unnecessary loops
        Range<ChronoLocalDate> searchRange = Range.between(
                logRange.fit(query.getSearchRange().getMinimum()),
                logRange.fit(query.getSearchRange().getMaximum())
        );

        ChronoLocalDate d = searchRange.getMaximum();

        short safeGuard = Short.MAX_VALUE / 2;

        // While within search range, query logs of specific day
        // and then move the date backwards by one
        while (searchRange.contains(d)) {
            --safeGuard;

            if (safeGuard < 0) {
                LOGGER.error(
                        "Query operation passed safeGuard loop limit! " +
                                "date={}, queryRange={}, searchDate={}",
                        date, searchRange, d,
                        new RuntimeException()
                );

                break;
            }

            LogContainer container;

            // If current date, then don't load file,
            // File will most likely have invalid data,
            // use the loaded container
            if (d.compareTo(date) == 0) {
                container = this.logs;
            } else {
                // If the file don't exist, nothing to look for
                if (!Files.exists(storage.getLogFile(d))) {
                    d = d.minus(1, ChronoUnit.DAYS);
                    continue;
                }

                // Load log file
                container = new LogContainer();
                storage.loadLogs(d, container);
            }

            // Perform query
            container.performQuery(query, builder, found);

            // If we've found more than the max requested results,
            // then stop looking for more
            if (found.get() >= query.getMaxResults()) {
                break;
            }

            d = d.minus(1, ChronoUnit.DAYS);
        }

        return builder.build();
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    @OnSave
    public void save() {
        storage.saveLogs(date, logs);
    }

    @OnLoad
    public void load() {
        logs = new LogContainer();
        storage.loadLogs(date, logs);

        LocalDate minDate = storage.findMinLog();
        logRange = Range.between(minDate, date);
    }
}