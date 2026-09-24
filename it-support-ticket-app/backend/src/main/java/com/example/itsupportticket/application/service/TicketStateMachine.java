package com.example.itsupportticket.application.service;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.itsupportticket.application.exception.BusinessRuleViolationException;
import com.example.itsupportticket.domain.enums.TicketStatus;

@Component
public class TicketStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.OPEN, Set.of(TicketStatus.ASSIGNED),
            TicketStatus.ASSIGNED, Set.of(TicketStatus.IN_PROGRESS),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED)
    );

    public void validateTransition(TicketStatus currentStatus, TicketStatus requestedStatus) {
        if (currentStatus == null || requestedStatus == null) {
            throw new BusinessRuleViolationException("Ticket status is required.");
        }

        Set<TicketStatus> allowedNextStatuses = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedNextStatuses.contains(requestedStatus)) {
            throw new BusinessRuleViolationException(
                    "Invalid status transition from " + currentStatus + " to " + requestedStatus + "."
            );
        }
    }
}
