package com.studypilot.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.studypilot.common.UnprocessableException;
import com.studypilot.plan.SessionScheduler.BusyWindow;

class SessionSchedulerTest {

    private static final LocalTime EVENING = LocalTime.of(18, 0);
    private static final LocalDate MON = LocalDate.of(2026, 1, 5);

    @Test
    void spreadsMoreTopicsThanDaysEvenlyAndSplitsTheWindow() {
        List<StudySession> sessions = SessionScheduler.schedule(List.of("a", "b", "c", "d", "e"), MON, MON.plusDays(1),
                Map.of(), 2, EVENING);

        assertThat(sessions).extracting(StudySession::getTopic).containsExactly("a", "b", "c", "d", "e");
        // 5 topics over 2 days: 3 then 2, each day's 120 minutes divided equally, back to back.
        assertThat(sessions.get(0).getStartTime()).isEqualTo(MON.atTime(18, 0));
        assertThat(sessions.get(0).duration().toMinutes()).isEqualTo(40);
        assertThat(sessions.get(2).getEndTime()).isEqualTo(MON.atTime(20, 0));
        assertThat(sessions.get(3).getStartTime()).isEqualTo(MON.plusDays(1).atTime(18, 0));
        assertThat(sessions.get(3).duration().toMinutes()).isEqualTo(60);
    }

    @Test
    void spreadsFewerTopicsThanDaysAcrossTheWholeRange() {
        List<StudySession> sessions = SessionScheduler.schedule(List.of("a", "b"), MON, MON.plusDays(9), Map.of(), 1, EVENING);

        assertThat(sessions).extracting(s -> s.getStartTime().toLocalDate()).containsExactly(MON, MON.plusDays(5));
    }

    @Test
    void crossesMonthBoundariesCorrectly() {
        // The original implementation used Period.getDays(), which returns 0 for Jan 31 -> Mar 1.
        List<StudySession> sessions = SessionScheduler.schedule(List.of("a", "b", "c"), LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 3, 1), Map.of(), 1, EVENING);

        assertThat(sessions).hasSize(3);
        assertThat(sessions.get(2).getStartTime().toLocalDate()).isAfter(LocalDate.of(2026, 2, 15));
    }

    @Test
    void movesTheWindowAfterABusyBlock() {
        BusyWindow lecture = new BusyWindow(MON.atTime(17, 0), MON.atTime(19, 30));

        List<StudySession> sessions = SessionScheduler.schedule(List.of("a"), MON, MON, Map.of(MON, lecture), 2, EVENING);

        assertThat(sessions.get(0).getStartTime()).isEqualTo(MON.atTime(19, 30));
        assertThat(sessions.get(0).getEndTime()).isEqualTo(MON.atTime(21, 30));
    }

    @Test
    void skipsADayWhenTheBusyBlockLeavesNoRoom() {
        BusyWindow allEvening = new BusyWindow(MON.atTime(17, 0), MON.atTime(22, 0));

        List<StudySession> sessions = SessionScheduler.schedule(List.of("a"), MON, MON.plusDays(1),
                Map.of(MON, allEvening), 2, EVENING);

        assertThat(sessions.get(0).getStartTime()).isEqualTo(MON.plusDays(1).atTime(18, 0));
    }

    @Test
    void ignoresBusyBlocksThatDoNotOverlap() {
        BusyWindow morning = new BusyWindow(MON.atTime(9, 0), MON.atTime(12, 0));

        assertThat(SessionScheduler.placeWindow(MON, morning, 2, EVENING)).isEqualTo(LocalDateTime.of(MON, EVENING));
    }

    @Test
    void rejectsARangeWithNoFreeTime() {
        BusyWindow allDay = new BusyWindow(MON.atStartOfDay(), MON.atTime(23, 59));

        assertThatThrownBy(() -> SessionScheduler.schedule(List.of("a"), MON, MON, Map.of(MON, allDay), 2, EVENING))
                .isInstanceOf(UnprocessableException.class);
    }

    @Test
    void rejectsAnEmptySyllabus() {
        assertThatThrownBy(() -> SessionScheduler.schedule(List.of(), MON, MON, Map.of(), 2, EVENING))
                .isInstanceOf(UnprocessableException.class);
    }
}
