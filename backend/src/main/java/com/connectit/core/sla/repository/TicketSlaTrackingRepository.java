package com.connectit.core.sla.repository;

import com.connectit.core.sla.entity.TicketSlaTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketSlaTrackingRepository extends JpaRepository<TicketSlaTracking, Long> {
    Optional<TicketSlaTracking> findByTicketId(Long ticketId);
    List<TicketSlaTracking> findByResponseDeadlineBeforeAndIsResponseBreachedFalseAndRespondedAtIsNull(LocalDateTime time);
    List<TicketSlaTracking> findByResolutionDeadlineBeforeAndIsResolutionBreachedFalseAndResolvedAtIsNull(LocalDateTime time);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM TicketSlaTracking t WHERE t.isResponseBreached = true OR t.isResolutionBreached = true")
    long countBreachedSlas();
}
