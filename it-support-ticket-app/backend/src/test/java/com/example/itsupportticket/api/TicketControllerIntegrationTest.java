package com.example.itsupportticket.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void createTicket_shouldReturnCreatedTicket() throws Exception {
        UserEntity employee = userRepository.save(new UserEntity("Alice", "alice@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-100", "Printer", "PRINTER", "Floor 1", employee, DeviceStatus.ACTIVE));

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", employee.getId())
                .header("X-User-Role", "EMPLOYEE")
                .content("{\"requesterUserId\":\"" + employee.getId() + "\",\"deviceId\":\"" + device.getId() + "\",\"title\":\"Printer not responding\",\"description\":\"The printer is offline\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Printer not responding"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createTicket_shouldRejectBlankTitle() throws Exception {
        UserEntity employee = userRepository.save(new UserEntity("Alice", "alice2@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-101", "Laptop", "LAPTOP", "Floor 2", employee, DeviceStatus.ACTIVE));

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", employee.getId())
                .header("X-User-Role", "EMPLOYEE")
                .content("{\"requesterUserId\":\"" + employee.getId() + "\",\"deviceId\":\"" + device.getId() + "\",\"title\":\"   \",\"description\":\"Broken\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Title is required."))
                .andExpect(jsonPath("$.path").value("/api/tickets"));
    }

    @Test
    void createTicket_shouldRejectMissingDescription() throws Exception {
        UserEntity employee = userRepository.save(new UserEntity("Alice", "alice-description@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-DESCRIPTION", "Laptop", "LAPTOP", "Floor 2", employee, DeviceStatus.ACTIVE));

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", employee.getId())
                .header("X-User-Role", "EMPLOYEE")
                .content("{\"requesterUserId\":\"" + employee.getId() + "\",\"deviceId\":\"" + device.getId() + "\",\"title\":\"Missing description\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Description is required."))
                .andExpect(jsonPath("$.path").value("/api/tickets"));
    }

    @Test
    void createTicket_shouldDefaultPriorityToMedium_whenPriorityIsMissing() throws Exception {
        UserEntity employee = userRepository.save(new UserEntity("Alice", "alice-default-priority@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-DEFAULT-PRIORITY", "Monitor", "MONITOR", "Floor 3", employee, DeviceStatus.ACTIVE));

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", employee.getId())
                .header("X-User-Role", "EMPLOYEE")
                .content("{\"requesterUserId\":\"" + employee.getId() + "\",\"deviceId\":\"" + device.getId() + "\",\"title\":\"Default priority\",\"description\":\"Priority was omitted\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createTicket_shouldRejectInvalidPriority() throws Exception {
        UserEntity employee = userRepository.save(new UserEntity("Alice", "alice3@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-102", "Monitor", "MONITOR", "Floor 3", employee, DeviceStatus.ACTIVE));

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", employee.getId())
                .header("X-User-Role", "EMPLOYEE")
                .content("{\"requesterUserId\":\"" + employee.getId() + "\",\"deviceId\":\"" + device.getId() + "\",\"title\":\"Bad priority\",\"description\":\"Broken\",\"priority\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    } 

    @Test
    void getTicket_shouldReturnTicketById() throws Exception {
        UserEntity employee = userRepository.save(new UserEntity("Alice", "alice4@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-103", "Camera", "CAMERA", "Floor 4", employee, DeviceStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(employee, device, "Camera failing", "Needs repair", Priority.MEDIUM));

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                .header("X-User-Id", employee.getId())
                .header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.getId().intValue()))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void getTicket_shouldReturnNotFound_whenMissing() throws Exception {
        mockMvc.perform(get("/api/tickets/999999")
                .header("X-User-Id", 1)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void assignTicket_shouldMoveToAssigned() throws Exception {
        UserEntity requester = userRepository.save(new UserEntity("Alice", "alice5@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-104", "Dock", "DOCK", "Floor 5", requester, DeviceStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(requester, device, "Dock issue", "Needs attention", Priority.HIGH));
        UserEntity lead = userRepository.save(new UserEntity("Lead", "lead@example.com", UserRole.TECH_LEAD, UserStatus.ACTIVE));
        UserEntity technician = userRepository.save(new UserEntity("Tech", "tech@example.com", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));

        mockMvc.perform(post("/api/tickets/{id}/assign", ticket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", lead.getId())
                .header("X-User-Role", "TECH_LEAD")
                .content("{\"technicianUserId\":\"" + technician.getId() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedTechnicianUserId").value(technician.getId().intValue()));
    }

    @Test
    void assignTicket_shouldRejectEmployeeAndPreserveOpenState() throws Exception {
        UserEntity employee = userRepository.save(new UserEntity("Alice", "alice-assign-employee@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-ASSIGN-EMPLOYEE", "Dock", "DOCK", "Floor 5", employee, DeviceStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(employee, device, "Dock issue", "Needs attention", Priority.HIGH));

        mockMvc.perform(post("/api/tickets/{id}/assign", ticket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", employee.getId())
                .header("X-User-Role", "EMPLOYEE")
                .content("{\"technicianUserId\":999999}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Only a Tech Lead can assign technicians."))
                .andExpect(jsonPath("$.path").value("/api/tickets/" + ticket.getId() + "/assign"));

        org.junit.jupiter.api.Assertions.assertEquals(TicketStatus.OPEN, ticket.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(ticket.getAssignedTechnician());
    }

    @Test
    void assignTicket_shouldRejectNonTechnicianAssigneeAndPreserveOpenState() throws Exception {
        UserEntity requester = userRepository.save(new UserEntity("Alice", "alice-non-technician@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-NON-TECHNICIAN", "Keyboard", "KEYBOARD", "Floor 6", requester, DeviceStatus.ACTIVE));
        UserEntity lead = userRepository.save(new UserEntity("Lead", "lead-non-technician@example.com", UserRole.TECH_LEAD, UserStatus.ACTIVE));
        UserEntity employee = userRepository.save(new UserEntity("Employee", "employee-assignee@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(requester, device, "Keyboard issue", "Needs repair", Priority.MEDIUM));

        mockMvc.perform(post("/api/tickets/{id}/assign", ticket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", lead.getId())
                .header("X-User-Role", "TECH_LEAD")
                .content("{\"technicianUserId\":" + employee.getId() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Only an IT technician can be assigned to a ticket."))
                .andExpect(jsonPath("$.path").value("/api/tickets/" + ticket.getId() + "/assign"));

        org.junit.jupiter.api.Assertions.assertEquals(TicketStatus.OPEN, ticket.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(ticket.getAssignedTechnician());
    }

    @Test
    void assignTicket_shouldRejectDuplicateAssignment() throws Exception {
        UserEntity requester = userRepository.save(new UserEntity("Alice", "alice6@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-105", "Keyboard", "KEYBOARD", "Floor 6", requester, DeviceStatus.ACTIVE));
        UserEntity lead = userRepository.save(new UserEntity("Lead", "lead2@example.com", UserRole.TECH_LEAD, UserStatus.ACTIVE));
        UserEntity technician = userRepository.save(new UserEntity("Tech", "tech2@example.com", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(requester, device, "Keyboard issue", "Needs repair", Priority.MEDIUM));
        ticket.setAssignedTechnician(technician);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticketRepository.save(ticket);

        mockMvc.perform(post("/api/tickets/{id}/assign", ticket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", lead.getId())
                .header("X-User-Role", "TECH_LEAD")
                .content("{\"technicianUserId\":\"" + technician.getId() + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Only OPEN tickets can be assigned."))
                .andExpect(jsonPath("$.path").value("/api/tickets/" + ticket.getId() + "/assign"));
    }

    @Test
    void startWork_shouldUpdateStatusToInProgress() throws Exception {
        UserEntity requester = userRepository.save(new UserEntity("Alice", "alice7@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-106", "Router", "ROUTER", "Floor 7", requester, DeviceStatus.ACTIVE));
        UserEntity technician = userRepository.save(new UserEntity("Tech", "tech3@example.com", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(requester, device, "Router issue", "No connection", Priority.HIGH));
        ticket.setAssignedTechnician(technician);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticketRepository.save(ticket);

        mockMvc.perform(post("/api/tickets/{id}/start", ticket.getId())
                .header("X-User-Id", technician.getId())
                .header("X-User-Role", "IT_TECHNICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void startWork_shouldRejectOpenTicketAndPreserveState() throws Exception {
        UserEntity requester = userRepository.save(new UserEntity("Alice", "alice-start-open@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-START-OPEN", "Router", "ROUTER", "Floor 7", requester, DeviceStatus.ACTIVE));
        UserEntity technician = userRepository.save(new UserEntity("Tech", "tech-start-open@example.com", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(requester, device, "Router issue", "No connection", Priority.HIGH));
        ticket.setAssignedTechnician(technician);
        ticketRepository.save(ticket);

        mockMvc.perform(post("/api/tickets/{id}/start", ticket.getId())
                .header("X-User-Id", technician.getId())
                .header("X-User-Role", "IT_TECHNICIAN"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Ticket must be ASSIGNED before work can start."))
                .andExpect(jsonPath("$.path").value("/api/tickets/" + ticket.getId() + "/start"));

        org.junit.jupiter.api.Assertions.assertEquals(TicketStatus.OPEN, ticket.getStatus());
    }

    @Test
    void resolveTicket_shouldUpdateStatusToResolved() throws Exception {
        UserEntity requester = userRepository.save(new UserEntity("Alice", "alice8@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-107", "Switch", "SWITCH", "Floor 8", requester, DeviceStatus.ACTIVE));
        UserEntity technician = userRepository.save(new UserEntity("Tech", "tech4@example.com", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(requester, device, "Switch issue", "No connectivity", Priority.MEDIUM));
        ticket.setAssignedTechnician(technician);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);

        mockMvc.perform(post("/api/tickets/{id}/resolve", ticket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", technician.getId())
                .header("X-User-Role", "IT_TECHNICIAN")
                .content("{\"resolutionNote\":\"Replaced failed port\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Replaced failed port"));
    }

    @Test
    void resolveTicket_shouldRejectBlankResolutionNote() throws Exception {
        UserEntity requester = userRepository.save(new UserEntity("Alice", "alice9@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-108", "Server", "SERVER", "Floor 9", requester, DeviceStatus.ACTIVE));
        UserEntity technician = userRepository.save(new UserEntity("Tech", "tech5@example.com", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));
        TicketEntity ticket = ticketRepository.save(new TicketEntity(requester, device, "Server issue", "Needs fix", Priority.LOW));
        ticket.setAssignedTechnician(technician);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);

        mockMvc.perform(post("/api/tickets/{id}/resolve", ticket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", technician.getId())
                .header("X-User-Role", "IT_TECHNICIAN")
                .content("{\"resolutionNote\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Resolution note is required."))
                .andExpect(jsonPath("$.path").value("/api/tickets/" + ticket.getId() + "/resolve"));
    }

    @Test
    void ticketLifecycle_shouldCreateAssignStartAndResolveTicket() throws Exception {
        UserEntity requester = userRepository.save(new UserEntity("Alice", "alice-lifecycle@example.com", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        DeviceEntity device = deviceRepository.save(new DeviceEntity("ASSET-LIFECYCLE", "Switch", "SWITCH", "Floor 8", requester, DeviceStatus.ACTIVE));
        UserEntity lead = userRepository.save(new UserEntity("Lead", "lead-lifecycle@example.com", UserRole.TECH_LEAD, UserStatus.ACTIVE));
        UserEntity technician = userRepository.save(new UserEntity("Tech", "tech-lifecycle@example.com", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));

        String createResponse = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", requester.getId())
                .header("X-User-Role", "EMPLOYEE")
                .content("{\"requesterUserId\":\"" + requester.getId() + "\",\"deviceId\":\"" + device.getId() + "\",\"title\":\"Lifecycle switch issue\",\"description\":\"The switch has no connectivity\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requesterUserId").value(requester.getId().intValue()))
                .andExpect(jsonPath("$.deviceId").value(device.getId().intValue()))
                .andExpect(jsonPath("$.title").value("Lifecycle switch issue"))
                .andExpect(jsonPath("$.description").value("The switch has no connectivity"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long ticketId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(post("/api/tickets/{id}/assign", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", lead.getId())
                .header("X-User-Role", "TECH_LEAD")
                .content("{\"technicianUserId\":" + technician.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedTechnicianUserId").value(technician.getId().intValue()));

        mockMvc.perform(post("/api/tickets/{id}/start", ticketId)
                .header("X-User-Id", technician.getId())
                .header("X-User-Role", "IT_TECHNICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/tickets/{id}/resolve", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", technician.getId())
                .header("X-User-Role", "IT_TECHNICIAN")
                .content("{\"resolutionNote\":\"Replaced the failed switch port\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.assignedTechnicianUserId").value(technician.getId().intValue()))
                .andExpect(jsonPath("$.resolutionNote").value("Replaced the failed switch port"));

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                .header("X-User-Id", requester.getId())
                .header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Replaced the failed switch port"));
    }

    @Test
    void malformedRequest_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 1)
                .header("X-User-Role", "EMPLOYEE")
                .content("{not valid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request."))
                .andExpect(jsonPath("$.path").value("/api/tickets"));
    }
}
