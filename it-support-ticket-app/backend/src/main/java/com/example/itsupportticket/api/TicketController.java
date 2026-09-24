package com.example.itsupportticket.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.itsupportticket.application.dto.AssignTicketRequest;
import com.example.itsupportticket.application.dto.CreateTicketRequest;
import com.example.itsupportticket.application.dto.ResolveTicketRequest;
import com.example.itsupportticket.application.dto.TicketResponse;
import com.example.itsupportticket.application.exception.ForbiddenOperationException;
import com.example.itsupportticket.application.service.TicketService;
import com.example.itsupportticket.domain.enums.UserRole;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/tickets")
    public ResponseEntity<TicketResponse> createTicket(
            @RequestBody CreateTicketRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        UserRole role = parseRole(userRole);
        TicketResponse response = ticketService.createTicket(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PostMapping("/tickets/{id}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable("id") Long id,
            @RequestBody AssignTicketRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        UserRole role = parseRole(userRole);
        return ResponseEntity.ok(ticketService.assignTechnician(id, request, userId, role));
    }

    @PostMapping("/tickets/{id}/start")
    public ResponseEntity<TicketResponse> startWork(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        UserRole role = parseRole(userRole);
        return ResponseEntity.ok(ticketService.startWork(id, userId, role));
    }

    @PostMapping("/tickets/{id}/resolve")
    public ResponseEntity<TicketResponse> resolveTicket(
            @PathVariable("id") Long id,
            @RequestBody ResolveTicketRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        UserRole role = parseRole(userRole);
        return ResponseEntity.ok(ticketService.resolveTicket(id, request, userId, role));
    }

    private UserRole parseRole(String userRole) {
        if (userRole == null || userRole.isBlank()) {
            throw new ForbiddenOperationException("User role header is required.");
        }
        try {
            return UserRole.valueOf(userRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ForbiddenOperationException("User role is invalid.");
        }
    }
}
