package com.studypilot.progress;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class ProgressServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    private static TreeSet<LocalDate> daysAgo(int... offsets) {
        TreeSet<LocalDate> days = new TreeSet<>();
        for (int o : offsets) {
            days.add(TODAY.minusDays(o));
        }
        return days;
    }

    @Test
    void currentStreakCountsBackFromToday() {
        assertThat(ProgressService.currentStreak(daysAgo(0, 1, 2, 5), TODAY)).isEqualTo(3);
    }

    @Test
    void currentStreakSurvivesUntilTodayIsOver() {
        // Not studied yet today, but yesterday and the day before were: the streak is still alive.
        assertThat(ProgressService.currentStreak(daysAgo(1, 2), TODAY)).isEqualTo(2);
    }

    @Test
    void currentStreakIsZeroOnceADayIsMissed() {
        assertThat(ProgressService.currentStreak(daysAgo(2, 3, 4), TODAY)).isZero();
    }

    @Test
    void longestStreakFindsTheBestRunAnywhereInHistory() {
        assertThat(ProgressService.longestStreak(daysAgo(0, 1, 5, 6, 7, 8, 20))).isEqualTo(4);
        assertThat(ProgressService.longestStreak(new TreeSet<>(List.of()))).isZero();
    }
}
