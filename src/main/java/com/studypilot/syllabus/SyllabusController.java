package com.studypilot.syllabus;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.studypilot.idempotency.IdempotencyService;
import com.studypilot.syllabus.SyllabusService.SyllabusUpload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/syllabus")
@Tag(name = "Syllabus", description = "Upload syllabus PDFs and browse the catalogue")
@Validated
public class SyllabusController {

    static final String USER_ID = "[A-Za-z0-9_-]{1,64}";

    private final SyllabusService service;
    private final IdempotencyService idempotency;

    public SyllabusController(SyllabusService service, IdempotencyService idempotency) {
        this.service = service;
        this.idempotency = idempotency;
    }

    @Operation(summary = "Upload a syllabus PDF; topics are extracted and stored")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SyllabusResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam @NotBlank @Size(max = 200) String title,
            @RequestParam @NotBlank @Size(max = 120) String faculty,
            @RequestParam @NotBlank @Size(max = 120) String department,
            @RequestParam @NotBlank @Size(max = 40) String semester,
            @RequestParam @Pattern(regexp = USER_ID) String userId,
            @RequestHeader(name = IdempotencyService.HEADER, required = false) String idempotencyKey) throws IOException {
        byte[] content = file.getBytes();
        String fingerprint = String.join("|", IdempotencyService.sha256(content), title, faculty, department, semester);
        return idempotency.execute("syllabus-upload:" + userId, idempotencyKey, fingerprint, SyllabusResponse.class,
                () -> service.upload(new SyllabusUpload(title, faculty, department, semester, userId,
                        file.getOriginalFilename(), content)));
    }

    @Operation(summary = "Get one syllabus")
    @GetMapping("/{id}")
    public SyllabusResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @Operation(summary = "List syllabi uploaded by a user")
    @GetMapping("/user/{userId}")
    public List<SyllabusResponse> forUser(@PathVariable @Pattern(regexp = USER_ID) String userId) {
        return service.forUser(userId);
    }

    @Operation(summary = "Browse the catalogue, optionally filtered")
    @GetMapping
    public List<SyllabusResponse> catalogue(@RequestParam(required = false) String faculty,
            @RequestParam(required = false) String department, @RequestParam(required = false) String semester) {
        return service.search(faculty, department, semester, null);
    }

    @Operation(summary = "Search by title or topic within an optional catalogue filter")
    @GetMapping("/search")
    public List<SyllabusResponse> search(@RequestParam(required = false) String faculty,
            @RequestParam(required = false) String department, @RequestParam(required = false) String semester,
            @RequestParam(required = false) @Size(max = 100) String search) {
        return service.search(faculty, department, semester, search);
    }
}
