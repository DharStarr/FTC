package net.forthecrown.log;

import lombok.Getter;
import net.forthecrown.core.AutoSave;
import net.forthecrown.core.DayChange;
import net.forthecrown.core.DayChangeListener;
import net.forthecrown.utils.io.PathUtil;
import org.apache.commons.lang3.Range;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Getter
public class DataManager implements DayChangeListener {
    @Getter
    private static final DataManager instance = new DataManager();

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

    private static void init() {
        instance.load();

        AutoSave.get().addCallback(instance::save);
        DayChange.get().addListener(instance);
    }

    /* ------------------------------ METHODS ------------------------------- */

    @Override
    public void onDayChange(ZonedDateTime time) {
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

        // While within search range, query logs of specific day
        // and then move the date backwards by one
        while (searchRange.contains(d)) {
            LogContainer container;

            // If current date, then don't load file,
            // File will most likely have invalid data,
            // use the loaded container
            if (d.equals(date)) {
                container = this.logs;
            } else {
                // If the file don't exist, nothing to look for
                if (!Files.exists(storage.getLogFile(d))) {
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

    public void save() {
        storage.saveLogs(date, logs);
    }

    public void load() {
        logs = new LogContainer();
        storage.loadLogs(date, logs);

        var minYear = storage.findMinLog();
        logRange = Range.between(
                LocalDate.of(minYear.getYear(), minYear.getMonth(), 1),
                date
        );
    }
}