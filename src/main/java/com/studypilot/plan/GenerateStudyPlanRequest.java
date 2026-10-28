package com.studypilot.plan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.studypilot.plan.SessionScheduler.BusyWindow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record GenerateStudyPlanRequest(
        @NotNull Long syllabusId,
        @NotNull @Pattern(regexp = "[A-Za-z0-9_-]{1,64}") String userId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Schema(description = "Busy blocks keyed by date; the day's study window moves after them")
        Map<LocalDate, BusyWindow> busyHours,
        @Min(1) @Max(8) Integer hoursPerDay,
        @Pattern(regexp = "(?i)(morning|afternoon|evening)?") String preferredTime) {

    static final int MAX_PLAN_DAYS = 180;

    @JsonIgnore
    @AssertTrue(message = "endDate must be on or after startDate and the plan at most 180 days long")
    public boolean isRangeValid() {
        return startDate == null || endDate == null
                || (!endDate.isBefore(startDate) && ChronoUnit.DAYS.between(startDate, endDate) < MAX_PLAN_DAYS);
    }

    int hoursPerDayOrDefault() {
        return hoursPerDay == null ? 2 : hoursPerDay;
    }

    Map<LocalDate, BusyWindow> busyHoursOrEmpty() {
        return busyHours == null ? Map.of() : busyHours;
    }

    LocalTime preferredStart() {
        String time = preferredTime == null ? "" : preferredTime.toLowerCase(Locale.ROOT);
        return switch (time) {
            case "morning" -> LocalTime.of(8, 0);
            case "afternoon" -> LocalTime.of(14, 0);
            default -> LocalTime.of(18, 0);
        };
    }
}
