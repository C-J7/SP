package com.studypilot.progress;

import java.time.LocalDate;
import java.util.List;

public record ProgressStats(int totalSessions, int completedSessions, int overallProgress, int currentStreak,
        int longestStreak, double hoursThisWeek, List<Day> dailyProgress, List<Week> streakProgress,
        List<Course> courses) {

    /** Hours of completed study on one day of the trailing week. */
    public record Day(LocalDate date, String label, double hours) {
    }

    /** Days with any completed study in one of the trailing four weeks; week 4 is the current one. */
    public record Week(int week, int days) {
    }

    public record Course(Long courseId, String title, int completed, int total, int percent) {
    }
}
