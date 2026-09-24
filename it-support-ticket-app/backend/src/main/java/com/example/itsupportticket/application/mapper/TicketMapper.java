package com.example.itsupportticket.application.mapper;

import org.springframework.stereotype.Component;

import com.example.itsupportticket.application.dto.TicketResponse;
import com.example.itsupportticket.domain.model.TicketEntity;

@Component
public class TicketMapper {

    public TicketResponse toResponse(TicketEntity entity) {
        TicketResponse response = new TicketResponse();
        response.setId(entity.getId());
        response.setTicketNumber(entity.getTicketNumber() != null ? entity.getTicketNumber() : "T-" + entity.getId());
        response.setRequesterUserId(entity.getRequester() != null ? entity.getRequester().getId() : null);
        response.setDeviceId(entity.getDevice() != null ? entity.getDevice().getId() : null);
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setPriority(entity.getPriority());
        response.setStatus(entity.getStatus());
        response.setAssignedTechnicianUserId(entity.getAssignedTechnician() != null ? entity.getAssignedTechnician().getId() : null);
        response.setResolutionNote(entity.getResolutionNote());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
