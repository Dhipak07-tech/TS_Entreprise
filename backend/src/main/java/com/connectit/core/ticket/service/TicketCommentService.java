package com.connectit.core.ticket.service;

import com.connectit.core.rbac.entity.Role;
import com.connectit.core.ticket.dto.AddCommentRequest;
import com.connectit.core.ticket.dto.TicketActivityDTO;
import com.connectit.core.ticket.dto.TicketCommentDTO;
import com.connectit.core.ticket.entity.Ticket;
import com.connectit.core.ticket.entity.TicketActivity;
import com.connectit.core.ticket.entity.TicketComment;
import com.connectit.core.ticket.repository.TicketActivityRepository;
import com.connectit.core.ticket.repository.TicketCommentRepository;
import com.connectit.core.ticket.repository.TicketRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.entity.UserProfile;
import com.connectit.core.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketCommentService {

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private TicketActivityRepository ticketActivityRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public List<TicketCommentDTO> getCommentsForTicket(Long ticketId, User user) {
        // If user is agent/admin, show all comments including internal ones, else show only public ones
        boolean isAgentOrAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMINISTRATOR") 
                        || r.getName().equals("SUPPORT_AGENT") 
                        || r.getName().equals("TEAM_LEAD")
                        || r.getName().equals("SUPER_ADMIN"));

        List<TicketComment> comments;
        if (isAgentOrAdmin) {
            comments = ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        } else {
            comments = ticketCommentRepository.findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(ticketId);
        }

        return comments.stream().map(this::mapToCommentDTO).collect(Collectors.toList());
    }

    @Transactional
    public TicketCommentDTO addComment(Long ticketId, AddCommentRequest request, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .author(user)
                .body(request.getBody())
                .isInternal(request.getIsInternal() != null && request.getIsInternal())
                .build();

        TicketComment saved = ticketCommentRepository.save(comment);

        // Log activity
        String desc = (comment.getIsInternal() ? "[Internal Note] " : "") + "Comment added by " + getDisplayName(user);
        logActivity(ticket, user, "COMMENT_ADDED", null, null, desc);

        return mapToCommentDTO(saved);
    }

    public List<TicketActivityDTO> getActivitiesForTicket(Long ticketId) {
        return ticketActivityRepository.findByTicketIdOrderByOccurredAtDesc(ticketId).stream()
                .map(this::mapToActivityDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void logActivity(Ticket ticket, User actor, String type, String oldVal, String newVal, String description) {
        TicketActivity activity = TicketActivity.builder()
                .ticket(ticket)
                .actor(actor)
                .activityType(type)
                .oldValue(oldVal)
                .newValue(newVal)
                .description(description)
                .build();
        ticketActivityRepository.save(activity);
    }

    private TicketCommentDTO mapToCommentDTO(TicketComment comment) {
        User author = comment.getAuthor();
        UserProfile profile = userProfileRepository.findByUserId(author.getId()).orElse(null);
        String name = profile != null && (profile.getFirstName() != null || profile.getLastName() != null)
                ? (profile.getFirstName() + " " + (profile.getLastName() != null ? profile.getLastName() : "")).trim()
                : author.getUsername();

        return TicketCommentDTO.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .authorName(name)
                .authorEmail(author.getEmail())
                .body(comment.getBody())
                .isInternal(comment.getIsInternal())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private TicketActivityDTO mapToActivityDTO(TicketActivity activity) {
        String actorName = "System";
        if (activity.getActor() != null) {
            User actor = activity.getActor();
            UserProfile profile = userProfileRepository.findByUserId(actor.getId()).orElse(null);
            actorName = profile != null && (profile.getFirstName() != null || profile.getLastName() != null)
                    ? (profile.getFirstName() + " " + (profile.getLastName() != null ? profile.getLastName() : "")).trim()
                    : actor.getUsername();
        }

        return TicketActivityDTO.builder()
                .id(activity.getId())
                .ticketId(activity.getTicket().getId())
                .actorName(actorName)
                .activityType(activity.getActivityType())
                .oldValue(activity.getOldValue())
                .newValue(activity.getNewValue())
                .description(activity.getDescription())
                .occurredAt(activity.getOccurredAt())
                .build();
    }

    private String getDisplayName(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        return profile != null && (profile.getFirstName() != null || profile.getLastName() != null)
                ? (profile.getFirstName() + " " + (profile.getLastName() != null ? profile.getLastName() : "")).trim()
                : user.getUsername();
    }
}
