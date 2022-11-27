package net.forthecrown.core.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

@Getter
@RequiredArgsConstructor
public enum ResetInterval {
    DAILY ("Daily") {
        @Override
        public int getMax() {
            return ChallengeConfig.maxDailyChallenges;
        }

        @Override
        public boolean areNeighbouring(LocalDate earlier,
                                       LocalDate later
        ) {
            var matchTest = later.minusDays(1);
            return matchTest.getDayOfYear() == earlier.getDayOfYear();
        }
    },

    WEEKLY ("Weekly") {
        @Override
        public int getMax() {
            return ChallengeConfig.maxWeeklyChallenges;
        }

        @Override
        public boolean areNeighbouring(LocalDate earlier,
                                       LocalDate later
        ) {
            var matchTest = later.minusWeeks(1);

            int week1 = matchTest.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
            int week2 = earlier.get(ChronoField.ALIGNED_WEEK_OF_YEAR);

            return week2 == week1;
        }
    },

    MANUAL ("") {
        @Override
        public int getMax() {
            return -1;
        }

        @Override
        public boolean shouldRefill() {
            return false;
        }

        @Override
        public boolean areNeighbouring(LocalDate d1,
                                       LocalDate d2
        ) {
            return false;
        }
    };

    private final String displayName;

    public abstract int getMax();

    public boolean shouldRefill() {
        return true;
    }

    public abstract boolean areNeighbouring(LocalDate earlier,
                                            LocalDate later
    );
}