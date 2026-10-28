package com.studypilot.progress;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studypilot.plan.StudyPlanRepository;
import com.studypilot.plan.StudySession;
import com.studypilot.progress.ProgressStats.Course;
import com.studypilot.progress.ProgressStats.Day;
import com.studypilot.progress.ProgressStats.Week;
import com.studypilot.syllabus.Syllabus;

@Service
public class ProgressService {

    private final StudyPlanRepository plans;
    private final Clock clock;

    public ProgressService(StudyPlanRepository plans, Clock clock) {
        this.plans = plans;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ProgressStats forUser(String userId) {
        return compute(plans.findSessionsByUser(userId), LocalDate.now(clock), clock);
    }

    static ProgressStats compute(List<StudySession> sessions, LocalDate today, Clock clock) {
        int total = sessions.size();
        List<StudySession> done = sessions.stream().filter(StudySession::isCompleted).toList();

        // A study day is the day the work was actually ticked off, in the server's zone.
        Map<LocalDate, Double> hoursByDay = new LinkedHashMap<>();
        for (StudySession s : done) {
            LocalDate day = s.getCompletedAt().atZone(clock.getZone()).toLocalDate();
            hoursByDay.merge(day, s.duration().toMinutes() / 60.0, Double::sum);
        }
        NavigableSet<LocalDate> activeDays = new TreeSet<>(hoursByDay.keySet());

        List<Day> week = new ArrayList<>(7);
        double hoursThisWeek = 0;
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            double h = round1(hoursByDay.getOrDefault(d, 0.0));
            hoursThisWeek += h;
            week.add(new Day(d, d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), h));
        }

        List<Week> weeks = new ArrayList<>(4);
        for (int w = 0; w < 4; w++) {
            LocalDate from = today.minusDays(27L - 7L * w);
            LocalDate to = from.plusDays(6);
            weeks.add(new Week(w + 1, activeDays.subSet(from, true, to, true).size()));
        }

        Map<Long, int[]> perCourse = new LinkedHashMap<>();
        Map<Long, String> titles = new LinkedHashMap<>();
        for (StudySession s : sessions) {
            Syllabus syllabus = s.getPlan().getSyllabus();
            titles.putIfAbsent(syllabus.getId(), syllabus.getTitle());
            int[] counts = perCourse.computeIfAbsent(syllabus.getId(), k -> new int[2]);
            counts[1]++;
            if (s.isCompleted()) {
                counts[0]++;
            }
        }
        List<Course> courses = perCourse.entrySet().stream()
                .map(e -> new Course(e.getKey(), titles.get(e.getKey()), e.getValue()[0], e.getValue()[1],
                        percent(e.getValue()[0], e.getValue()[1])))
                .toList();

        return new ProgressStats(total, done.size(), percent(done.size(), total), currentStreak(activeDays, today),
                longestStreak(activeDays), round1(hoursThisWeek), week, weeks, courses);
    }

    /** Consecutive study days ending today, or ending yesterday if today has not been studied yet. */
    static int currentStreak(NavigableSet<LocalDate> days, LocalDate today) {
        LocalDate cursor = days.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    static int longestStreak(NavigableSet<LocalDate> days) {
        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate d : days) {
            run = previous != null && previous.plusDays(1).equals(d) ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = d;
        }
        return longest;
    }

    private static int percent(int part, int whole) {
        return whole == 0 ? 0 : Math.round(part * 100f / whole);
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
