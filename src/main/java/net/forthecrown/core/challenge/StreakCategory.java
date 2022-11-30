package net.forthecrown.core.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

/**
 * A category for streaks, each challenge selects its own category.
 * <p>
 * For a player to get a streak in a category, it must complete all challenges
 * in a streak category in an allowed time frame.
 */
@RequiredArgsConstructor
public enum StreakCategory {
    /** Challenges which reset daily */
    DAILY ("Daily") {
        @Override
        public boolean areNeighboring(LocalDate later, LocalDate earlier) {
            var matchTest = later.minusDays(1);
            return matchTest.getDayOfYear() == earlier.getDayOfYear();
        }
    },

    /** Challenges which reset weekly */
    WEEKLY ("Weekly") {
        @Override
        public boolean areNeighboring(LocalDate later, LocalDate earlier) {
            var matchTest = later.minusWeeks(1);

            int week1 = matchTest.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
            int week2 = earlier.get(ChronoField.ALIGNED_WEEK_OF_YEAR);

            return week2 == week1;
        }
    },

    /** /shop challenges, reset daily */
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