package net.forthecrown.log;

import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;

public class LogIndex {
    private LocalDate idStart = LocalDate.now();

    public int getDateIndex(LocalDate date) {
        if (idStart.compareTo(date) == 0) {
            return 0;
        }

        long startDay = idStart.toEpochDay();
        long endDay = date.toEpochDay();

        return (int) (endDay - startDay);
    }

    public Range<ChronoLocalDate> getActiveTime() {
        return Range.between(idStart, LocalDate.now());
    }
}