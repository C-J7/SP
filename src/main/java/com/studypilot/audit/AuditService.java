package com.studypilot.audit;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.studypilot.common.RequestIdFilter;

@Service
public class AuditService {

    private final AuditEventRepository repository;
    private final Clock clock;

    public AuditService(AuditEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Joins the caller's transaction: the audit row commits or rolls back together with the change it describes. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String actor, String action, String entityType, Object entityId, String details) {
        repository.save(new AuditEvent(clock.instant(), actor, action, entityType, String.valueOf(entityId),
                RequestIdFilter.current(), details));
    }
}
