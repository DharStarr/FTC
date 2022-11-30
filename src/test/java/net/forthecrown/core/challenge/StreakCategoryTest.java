package net.forthecrown.core.challenge;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class StreakCategoryTest {

    @Test
    void areNeighboring() {
        LocalDate dayLater = LocalDate.now();
        LocalDate dayEarlier = dayLater.minusDays(1);

        LocalDate dayEarlier2 = dayLater.minusDays(2);

        assertTrue(StreakCategory.DAILY.areNeighboring(dayLater, dayEarlier));
        assertFalse(StreakCategory.DAILY.areNeighboring(dayEarlier, dayLater));

        assertTrue(StreakCategory.DAILY.areNeighboring(dayEarlier, dayEarlier2));
        assertFalse(StreakCategory.DAILY.areNeighboring(dayLater, dayEarlier2));
    }

    @Test
    void areNeighboringWeek() {
        LocalDate now = LocalDate.now();
        LocalDate weekAgo = now.minusWeeks(1);
        LocalDate weekAgo2 = now.minusWeeks(2);

        assertTrue(StreakCategory.WEEKLY.areNeighboring(now, weekAgo));
        assertTrue(StreakCategory.WEEKLY.areNeighboring(weekAgo, weekAgo2));
        assertFalse(StreakCategory.WEEKLY.areNeighboring(now, weekAgo2));
    }
}