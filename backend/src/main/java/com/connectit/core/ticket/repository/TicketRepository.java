package com.connectit.core.ticket.repository;

import com.connectit.core.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    List<Ticket> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    List<Ticket> findByAssignedUserIdOrderByCreatedAtDesc(Long assignedUserId);
    List<Ticket> findByAssignedTeamIdOrderByCreatedAtDesc(Long assignedTeamId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'OPEN'")
    long countOpenTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'RESOLVED' AND t.updatedAt >= CURRENT_DATE")
    long countResolvedToday();

    long countByAssignedUserId(Long userId);
    long countByRequesterId(Long userId);
    long countByStatus(String status);
    long countByStatusIn(java.util.Collection<String> statuses);
}
