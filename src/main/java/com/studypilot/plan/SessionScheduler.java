package com.studypilot.plan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.studypilot.common.UnprocessableException;

/**
 * Pure scheduling logic, free of Spring and the database so it can be tested exhaustively.
 *
 * <p>Each day gets one study window of {@code hoursPerDay} starting at the preferred time. If a busy block
 * overlaps it, the window slides to start when the busy block ends; if it would then run past the latest
 * allowed end, the day is skipped. Topics are spread evenly over the usable days and each day's window is
 * divided equally between that day's topics.
 */
public final class SessionScheduler {

    static final LocalTime LATEST_END = LocalTime.of(23, 0);

    private SessionScheduler() {
    }

    public static List<StudySession> schedule(List<String> topics, LocalDate start, LocalDate end,
            Map<LocalDate, BusyWindow> busy, int hoursPerDay, LocalTime preferredStart) {
        if (topics.isEmpty()) {
            throw new UnprocessableException("Syllabus has no topics to schedule");
        }
        List<LocalDateTime> windowStarts = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            LocalDateTime windowStart = placeWindow(day, busy.get(day), hoursPerDay, preferredStart);
            if (windowStart != null) {
                windowStarts.add(windowStart);
            }
        }
        if (windowStarts.isEmpty()) {
            throw new UnprocessableException("No free time between " + start + " and " + end + " to study");
        }

        int days = windowStarts.size();
        int n = topics.size();
        List<List<String>> perDay = new ArrayList<>(days);
        for (int d = 0; d < days; d++) {
            perDay.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            // Even spread whether there are more topics than days or more days than topics.
            perDay.get((int) ((long) i * days / n)).add(topics.get(i));
        }

        List<StudySession> sessions = new ArrayList<>(n);
        long windowMinutes = hoursPerDay * 60L;
        for (int d = 0; d < days; d++) {
            List<String> dayTopics = perDay.get(d);
            if (dayTopics.isEmpty()) {
                continue;
            }
            long slot = windowMinutes / dayTopics.size();
            LocalDateTime cursor = windowStarts.get(d);
            for (String topic : dayTopics) {
                sessions.add(new StudySession(topic, cursor, cursor.plusMinutes(slot)));
                cursor = cursor.plusMinutes(slot);
            }
        }
        return sessions;
    }

    /** Returns when the day's window starts, or null when the day has no room. */
    static LocalDateTime placeWindow(LocalDate day, BusyWindow busy, int hoursPerDay, LocalTime preferredStart) {
        LocalDateTime start = day.atTime(preferredStart);
        if (busy != null && busy.overlaps(start, start.plusHours(hoursPerDay))) {
            start = busy.end().truncatedTo(ChronoUnit.MINUTES);
        }
        LocalDateTime end = start.plusHours(hoursPerDay);
        return end.toLocalDate().equals(day) && !end.toLocalTime().isAfter(LATEST_END) ? start : null;
    }

    public record BusyWindow(LocalDateTime start, LocalDateTime end) {
        boolean overlaps(LocalDateTime from, LocalDateTime to) {
            return start.isBefore(to) && end.isAfter(from);
        }
    }
}
