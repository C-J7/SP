package com.studypilot.plan;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypilot.common.UnprocessableException;
import com.studypilot.idempotency.IdempotencyService;
import com.studypilot.plan.StudyPlanService.UpcomingSession;
import com.studypilot.progress.ProgressService;
import com.studypilot.progress.ProgressStats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/study-plan")
@Tag(name = "Study Plan", description = "Generate, track and complete study plans")
@Validated
public class StudyPlanController {

    private static final String USER_ID = "[A-Za-z0-9_-]{1,64}";

    private final StudyPlanService service;
    private final ProgressService progress;
    private final IdempotencyService idempotency;
    private final ObjectMapper objectMapper;

    public StudyPlanController(StudyPlanService service, ProgressService progress, IdempotencyService idempotency,
            ObjectMapper objectMapper) {
        this.service = service;
        this.progress = progress;
        this.idempotency = idempotency;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Generate a study plan from a syllabus",
            description = "Send an Idempotency-Key header to make retries safe: a replay returns the original plan.")
    @PostMapping("/generate")
    public ResponseEntity<StudyPlanResponse> generate(@Valid @RequestBody GenerateStudyPlanRequest request,
            @Parameter(description = "Unique key per logical request (8-100 chars)")
            @RequestHeader(name = IdempotencyService.HEADER, required = false) String idempotencyKey) {
        return idempotency.execute("study-plan:" + request.userId(), idempotencyKey, fingerprint(request),
                StudyPlanResponse.class, () -> service.generate(request));
    }

    @Operation(summary = "Get a study plan; the ETag is its version")
    @GetMapping("/{id}")
    public ResponseEntity<StudyPlanResponse> get(@PathVariable Long id) {
        StudyPlanResponse plan = service.get(id);
        return ResponseEntity.ok().eTag(Long.toString(plan.version())).body(plan);
    }

    @Operation(summary = "List a user's study plans")
    @GetMapping("/user/{userId}")
    public List<StudyPlanResponse> forUser(@PathVariable @Pattern(regexp = USER_ID) String userId) {
        return service.forUser(userId);
    }

    @Operation(summary = "Next incomplete sessions for a user")
    @GetMapping("/user/{userId}/upcoming")
    public List<UpcomingSession> upcoming(@PathVariable @Pattern(regexp = USER_ID) String userId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit) {
        return service.upcoming(userId, limit);
    }

    @Operation(summary = "Progress, streaks and per-course completion for a user")
    @GetMapping("/user/{userId}/progress")
    public ProgressStats progress(@PathVariable @Pattern(regexp = USER_ID) String userId) {
        return progress.forUser(userId);
    }

    @Operation(summary = "Mark a session complete or incomplete",
            description = "Send If-Match with the plan's ETag to reject the write if someone else changed the plan.")
    @PutMapping("/{studyPlanId}/session/{sessionId}")
    public ResponseEntity<StudyPlanResponse> setCompleted(@PathVariable Long studyPlanId, @PathVariable Long sessionId,
            @RequestParam(defaultValue = "true") boolean completed,
            @RequestHeader(name = "If-Match", required = false) String ifMatch) {
        StudyPlanResponse plan = service.setSessionCompleted(studyPlanId, sessionId, completed, parseETag(ifMatch));
        return ResponseEntity.ok().eTag(Long.toString(plan.version())).body(plan);
    }

    @Operation(summary = "Delete a study plan")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String fingerprint(GenerateStudyPlanRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Long parseETag(String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) {
            return null;
        }
        String value = ifMatch.strip().replaceFirst("^W/", "").replace("\"", "");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new UnprocessableException("If-Match must be an ETag returned by this API");
        }
    }
}
