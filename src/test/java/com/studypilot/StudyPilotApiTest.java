package com.studypilot;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Exercises the real HTTP surface against the real Flyway schema (H2 in PostgreSQL mode). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudyPilotApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    String user;
    long syllabusId;

    @BeforeEach
    void uploadSyllabus() throws Exception {
        user = "u-" + UUID.randomUUID().toString().substring(0, 8);
        byte[] pdf = TestPdfs.withLines("Outline", "- Arrays", "- Trees", "- Graphs", "- Heaps");
        String body = mvc.perform(multipart("/api/syllabus/upload")
                        .file(new MockMultipartFile("file", "outline.pdf", "application/pdf", pdf))
                        .param("title", "CSC 301").param("faculty", "Science").param("department", "Computer Science")
                        .param("semester", "First").param("userId", user))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.topics", hasSize(4)))
                .andExpect(jsonPath("$.sha256").value(matchesPattern("[0-9a-f]{64}")))
                .andReturn().getResponse().getContentAsString();
        syllabusId = json.readTree(body).get("id").asLong();
    }

    private String planRequest(LocalDate start, LocalDate end) {
        return """
                {"syllabusId": %d, "userId": "%s", "startDate": "%s", "endDate": "%s",
                 "hoursPerDay": "2", "preferredTime": "morning", "busyHours": {}}
                """.formatted(syllabusId, user, start, end);
    }

    @Test
    void retriedGenerateWithSameIdempotencyKeyReturnsTheSamePlanOnce() throws Exception {
        String key = UUID.randomUUID().toString();
        String request = planRequest(LocalDate.now(), LocalDate.now().plusDays(3));

        String first = mvc.perform(post("/api/study-plan/generate").header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "false"))
                .andReturn().getResponse().getContentAsString();

        String replay = mvc.perform(post("/api/study-plan/generate").header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "true"))
                .andReturn().getResponse().getContentAsString();

        long planId = json.readTree(first).get("id").asLong();
        org.assertj.core.api.Assertions.assertThat(json.readTree(replay).get("id").asLong()).isEqualTo(planId);
        mvc.perform(get("/api/study-plan/user/{u}", user)).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void reusingAnIdempotencyKeyWithADifferentPayloadIsRejected() throws Exception {
        String key = UUID.randomUUID().toString();
        mvc.perform(post("/api/study-plan/generate").header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(planRequest(LocalDate.now(), LocalDate.now().plusDays(3))))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/study-plan/generate").header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(planRequest(LocalDate.now(), LocalDate.now().plusDays(9))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(matchesPattern(".*different request payload.*")));
    }

    @Test
    void invalidRequestsReturnProblemDetailsWithTheRequestId() throws Exception {
        mvc.perform(post("/api/study-plan/generate").header("X-Request-Id", "trace-12345678")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planRequest(LocalDate.now(), LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(header().string("X-Request-Id", "trace-12345678"))
                .andExpect(jsonPath("$.requestId").value("trace-12345678"))
                .andExpect(jsonPath("$.errors.rangeValid").exists());
    }

    @Test
    void unknownResourcesAre404ProblemsAndNeverLeakInternals() throws Exception {
        mvc.perform(get("/api/study-plan/{id}", 987654))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void completingASessionUpdatesProgressBumpsTheETagAndIsAudited() throws Exception {
        JsonNode plan = json.readTree(mvc.perform(post("/api/study-plan/generate")
                        .contentType(MediaType.APPLICATION_JSON).content(planRequest(LocalDate.now(), LocalDate.now().plusDays(3))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long planId = plan.get("id").asLong();
        long sessionId = plan.get("sessions").get(0).get("id").asLong();
        long version = plan.get("version").asLong();

        mvc.perform(put("/api/study-plan/{p}/session/{s}", planId, sessionId).param("completed", "true")
                        .header("If-Match", "\"" + version + "\""))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"" + (version + 1) + "\""))
                .andExpect(jsonPath("$.completedSessions").value(1));

        // A writer holding the old ETag is refused instead of overwriting.
        mvc.perform(put("/api/study-plan/{p}/session/{s}", planId, sessionId).param("completed", "false")
                        .header("If-Match", "\"" + version + "\""))
                .andExpect(status().isPreconditionFailed());

        mvc.perform(get("/api/study-plan/user/{u}/progress", user))
                .andExpect(jsonPath("$.completedSessions").value(1))
                .andExpect(jsonPath("$.overallProgress").value(25))
                .andExpect(jsonPath("$.currentStreak").value(1))
                .andExpect(jsonPath("$.courses[0].percent").value(25));

        mvc.perform(get("/api/audit/user/{u}", user))
                .andExpect(jsonPath("$.content[0].action").value("SESSION_COMPLETED"))
                .andExpect(jsonPath("$.content[1].action").value("STUDY_PLAN_GENERATED"))
                .andExpect(jsonPath("$.content[2].action").value("SYLLABUS_UPLOADED"));
    }

    @Test
    void uploadRejectsANonPdfDisguisedWithAPdfName() throws Exception {
        mvc.perform(multipart("/api/syllabus/upload")
                        .file(new MockMultipartFile("file", "evil.pdf", "application/pdf", "<script/>".getBytes()))
                        .param("title", "x").param("faculty", "x").param("department", "x")
                        .param("semester", "x").param("userId", user))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void healthAndOpenApiAreExposed() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.info.title").value("StudyPilot API"));
    }
}
