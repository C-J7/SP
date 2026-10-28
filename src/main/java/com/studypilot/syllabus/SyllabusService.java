package com.studypilot.syllabus;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.studypilot.audit.AuditService;
import com.studypilot.common.NotFoundException;
import com.studypilot.idempotency.IdempotencyService;

@Service
public class SyllabusService {

    private final SyllabusRepository repository;
    private final TopicExtractor topicExtractor;
    private final AuditService audit;
    private final Clock clock;

    public SyllabusService(SyllabusRepository repository, TopicExtractor topicExtractor, AuditService audit, Clock clock) {
        this.repository = repository;
        this.topicExtractor = topicExtractor;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public SyllabusResponse upload(SyllabusUpload upload) {
        List<String> topics = topicExtractor.extract(upload.content());
        String filename = StringUtils.cleanPath(upload.originalFilename() == null ? "syllabus.pdf" : upload.originalFilename());
        Syllabus saved = repository.save(new Syllabus(upload.title().strip(), upload.faculty().strip(),
                upload.department().strip(), upload.semester().strip(), filename,
                IdempotencyService.sha256(upload.content()), upload.userId(), clock.instant(), topics));
        audit.record(upload.userId(), "SYLLABUS_UPLOADED", "Syllabus", saved.getId(),
                topics.size() + " topics extracted from " + filename);
        return SyllabusResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SyllabusResponse get(Long id) {
        return repository.findById(id).map(SyllabusResponse::from).orElseThrow(() -> new NotFoundException("Syllabus", id));
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> forUser(String userId) {
        return repository.findByUploadedByOrderByUploadedAtDesc(userId).stream().map(SyllabusResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> search(String faculty, String department, String semester, String query) {
        String q = StringUtils.hasText(query) ? query.strip().toLowerCase(Locale.ROOT) : null;
        return repository.findCatalogue(blankToNull(faculty), blankToNull(department), blankToNull(semester)).stream()
                .filter(s -> q == null
                        || s.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || s.getTopics().stream().anyMatch(t -> t.toLowerCase(Locale.ROOT).contains(q)))
                .map(SyllabusResponse::from)
                .toList();
    }

    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s.strip() : null;
    }

    public record SyllabusUpload(String title, String faculty, String department, String semester, String userId,
            String originalFilename, byte[] content) {
    }
}
