package net.forthecrown.core.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

@RequiredArgsConstructor
public enum StreakCategory {
    DAILY ("Daily") {
        @Override
        public boolean areNeighboring(LocalDate later, LocalDate earlier) {
            var matchTest = later.minusDays(1);
            return matchTest.getDayOfYear() == earlier.getDayOfYear();
        }
    },

    WEEKLY ("Weekly") {
        @Override
        public boolean areNeighboring(LocalDate later, LocalDate earlier) {
            var matchTest = later.minusWeeks(1);

            int week1 = matchTest.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
            int week2 = earlier.get(ChronoField.ALIGNED_WEEK_OF_YEAR);

            return week2 == week1;
        }
    },

    ITEMS ("Item") {
        @Override
        public boolean areNeighboring(LocalDate later, LocalDate earlier) {
            return DAILY.areNeighboring(later, earlier);
        }
    };

    @Getter
    private final String displayName;

    public abstract boolean areNeighboring(LocalDate later, LocalDate earlier);
}