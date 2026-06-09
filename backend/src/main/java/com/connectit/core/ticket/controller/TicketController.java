package com.connectit.core.ticket.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.config.security.services.UserDetailsImpl;
import com.connectit.core.ticket.dto.CreateTicketRequest;
import com.connectit.core.ticket.dto.TicketResponse;
import com.connectit.core.ticket.dto.TicketDetailDTO;
import com.connectit.core.ticket.dto.TicketCommentDTO;
import com.connectit.core.ticket.dto.TicketActivityDTO;
import com.connectit.core.ticket.dto.AddCommentRequest;
import com.connectit.core.ticket.service.TicketService;
import com.connectit.core.ticket.service.TicketCommentService;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User requester = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
        TicketResponse response = ticketService.createTicket(request, requester);
        return ResponseEntity.ok(ApiResponse.success("Ticket created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
        return ResponseEntity.ok(ApiResponse.success(ticketService.getMyTickets(user, status, search)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ASSIGN_TICKETS','MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        TicketResponse response = ticketService.updateTicketStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated successfully", response));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('ASSIGN_TICKETS','MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long agentId) {
        TicketResponse response = ticketService.assignTicket(id, teamId, agentId);
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully", response));
    }

    @Autowired
    private TicketCommentService ticketCommentService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDetailDTO>> getTicketDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketDetail(id)));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<TicketCommentDTO>>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
        return ResponseEntity.ok(ApiResponse.success(ticketCommentService.getCommentsForTicket(id, user)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TicketCommentDTO>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", ticketCommentService.addComment(id, request, user)));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<List<TicketActivityDTO>>> getActivities(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ticketCommentService.getActivitiesForTicket(id)));
    }
}
