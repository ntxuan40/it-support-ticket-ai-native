package com.example.itsupportticket.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.itsupportticket.application.dto.AssignTicketRequest;
import com.example.itsupportticket.application.dto.CreateTicketRequest;
import com.example.itsupportticket.application.dto.ResolveTicketRequest;
import com.example.itsupportticket.application.dto.TicketResponse;
import com.example.itsupportticket.application.exception.BusinessRuleViolationException;
import com.example.itsupportticket.application.exception.ForbiddenOperationException;
import com.example.itsupportticket.application.exception.ResourceNotFoundException;
import com.example.itsupportticket.application.exception.ValidationException;
import com.example.itsupportticket.application.mapper.TicketMapper;
import com.example.itsupportticket.domain.enums.DeviceStatus;
import com.example.itsupportticket.domain.enums.Priority;
import com.example.itsupportticket.domain.enums.TicketStatus;
import com.example.itsupportticket.domain.enums.UserRole;
import com.example.itsupportticket.domain.enums.UserStatus;
import com.example.itsupportticket.domain.model.DeviceEntity;
import com.example.itsupportticket.domain.model.TicketEntity;
import com.example.itsupportticket.domain.model.UserEntity;
import com.example.itsupportticket.domain.repository.DeviceRepository;
import com.example.itsupportticket.domain.repository.TicketRepository;
import com.example.itsupportticket.domain.repository.UserRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final TicketMapper ticketMapper;
    private final TicketStateMachine ticketStateMachine;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            DeviceRepository deviceRepository,
            TicketMapper ticketMapper,
            TicketStateMachine ticketStateMachine
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.ticketMapper = ticketMapper;
        this.ticketStateMachine = ticketStateMachine;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, Long actingUserId, UserRole actingRole) {
        validateCreateRequest(request);
        if (actingUserId == null || actingRole == null) {
            throw new ForbiddenOperationException("User identity is required to create a ticket.");
        }
        if (!isAllowedToCreateTicket(actingRole)) {
            throw new ForbiddenOperationException("Only an employee or administrator may create tickets.");
        }

        UserEntity requester = userRepository.findById(request.getRequesterUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found."));
        if (requester.getStatus() != UserStatus.ACTIVE) {
            throw new ValidationException("Requester must be active.");
        }

        DeviceEntity device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found."));
        if (device.getStatus() != DeviceStatus.ACTIVE) {
            throw new ValidationException("Device must be active.");
        }

        Priority priority = request.getPriority() != null ? request.getPriority() : Priority.MEDIUM;
        TicketEntity ticket = new TicketEntity(requester, device, request.getTitle().trim(), request.getDescription().trim(), priority);
        ticket.setTicketNumber("T-" + System.currentTimeMillis());
        ticket.setPriority(priority);

        TicketEntity saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long ticketId) {
        return getTicketById(ticketId, null, null);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long ticketId, Long actingUserId, UserRole actingRole) {
        if (actingUserId == null || actingRole == null) {
            throw new ForbiddenOperationException("User identity is required.");
        }
        if (!isAllowedToViewTicket(actingRole)) {
            throw new ForbiddenOperationException("User is not permitted to view tickets.");
        }

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found."));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse assignTechnician(Long ticketId, AssignTicketRequest request, Long actingUserId, UserRole actingRole) {
        if (actingUserId == null || actingRole == null) {
            throw new ForbiddenOperationException("User identity is required.");
        }
        if (actingRole != UserRole.TECH_LEAD) {
            throw new ForbiddenOperationException("Only a Tech Lead can assign technicians.");
        }

        if (request == null || request.getTechnicianUserId() == null) {
            throw new ValidationException("Technician user id is required.");
        }

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found."));
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new BusinessRuleViolationException("Only OPEN tickets can be assigned.");
        }
        if (ticket.getAssignedTechnician() != null) {
            throw new BusinessRuleViolationException("Ticket already has an assigned technician.");
        }

        UserEntity technician = userRepository.findById(request.getTechnicianUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found."));
        if (technician.getStatus() != UserStatus.ACTIVE) {
            throw new ValidationException("Technician must be active.");
        }
        if (technician.getRole() != UserRole.IT_TECHNICIAN) {
            throw new BusinessRuleViolationException("Only an IT technician can be assigned to a ticket.");
        }

        ticket.setAssignedTechnician(technician);
        ticketStateMachine.validateTransition(ticket.getStatus(), TicketStatus.ASSIGNED);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setUpdatedAt(Instant.now());

        TicketEntity saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse startWork(Long ticketId, Long actingUserId, UserRole actingRole) {
        validateActorIdentity(actingUserId, actingRole);
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found."));
        if (ticket.getAssignedTechnician() == null || !ticket.getAssignedTechnician().getId().equals(actingUserId)) {
            throw new ForbiddenOperationException("Only the assigned technician may start work.");
        }
        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            throw new BusinessRuleViolationException("Ticket must be ASSIGNED before work can start.");
        }

        ticketStateMachine.validateTransition(ticket.getStatus(), TicketStatus.IN_PROGRESS);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(Instant.now());
        TicketEntity saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse resolveTicket(Long ticketId, ResolveTicketRequest request, Long actingUserId, UserRole actingRole) {
        validateActorIdentity(actingUserId, actingRole);
        if (request == null || request.getResolutionNote() == null || request.getResolutionNote().isBlank()) {
            throw new ValidationException("Resolution note is required.");
        }

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found."));
        if (ticket.getAssignedTechnician() == null || !ticket.getAssignedTechnician().getId().equals(actingUserId)) {
            throw new ForbiddenOperationException("Only the assigned technician may resolve a ticket.");
        }
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new BusinessRuleViolationException("Ticket must be IN_PROGRESS before it can be resolved.");
        }

        ticketStateMachine.validateTransition(ticket.getStatus(), TicketStatus.RESOLVED);
        ticket.setResolutionNote(request.getResolutionNote().trim());
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setUpdatedAt(Instant.now());
        TicketEntity saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    private void validateCreateRequest(CreateTicketRequest request) {
        if (request == null) {
            throw new ValidationException("Ticket request is required.");
        }
        if (request.getRequesterUserId() == null) {
            throw new ValidationException("Requester user id is required.");
        }
        if (request.getDeviceId() == null) {
            throw new ValidationException("Device id is required.");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ValidationException("Title is required.");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ValidationException("Description is required.");
        }
    }

    private boolean isAllowedToCreateTicket(UserRole role) {
        return role == UserRole.EMPLOYEE || role == UserRole.ADMIN;
    }

    private boolean isAllowedToViewTicket(UserRole role) {
        return role == UserRole.EMPLOYEE || role == UserRole.TECH_LEAD || role == UserRole.IT_TECHNICIAN || role == UserRole.ADMIN;
    }

    private void validateActorIdentity(Long actingUserId, UserRole actingRole) {
        if (actingUserId == null || actingRole == null) {
            throw new ForbiddenOperationException("User identity is required.");
        }
        if (actingRole != UserRole.IT_TECHNICIAN && actingRole != UserRole.ADMIN && actingRole != UserRole.TECH_LEAD) {
            throw new ForbiddenOperationException("User is not permitted to perform this action.");
        }
    }
}
