package com.example.itsupportticket.infrastructure.demo;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.itsupportticket.config.DemoDataProperties;
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

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final DemoDataProperties demoDataProperties;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final TicketRepository ticketRepository;

    public DemoDataInitializer(
            DemoDataProperties demoDataProperties,
            UserRepository userRepository,
            DeviceRepository deviceRepository,
            TicketRepository ticketRepository
    ) {
        this.demoDataProperties = demoDataProperties;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!demoDataProperties.isEnabled()) {
            return;
        }

        if (userRepository.count() > 0 || deviceRepository.count() > 0 || ticketRepository.count() > 0) {
            return;
        }

        UserEntity alice = userRepository.save(new UserEntity("Alice Employee", "alice@company.local", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        UserEntity bob = userRepository.save(new UserEntity("Bob Employee", "bob@company.local", UserRole.EMPLOYEE, UserStatus.ACTIVE));
        UserEntity sam = userRepository.save(new UserEntity("Sam Tech Lead", "sam@company.local", UserRole.TECH_LEAD, UserStatus.ACTIVE));
        UserEntity jenny = userRepository.save(new UserEntity("Jenny Technician", "jenny@company.local", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));
        UserEntity marco = userRepository.save(new UserEntity("Marco Technician", "marco@company.local", UserRole.IT_TECHNICIAN, UserStatus.ACTIVE));

        DeviceEntity printer = deviceRepository.save(new DeviceEntity("PRN-1001", "Office Printer", "Printer", "Floor 1", alice, DeviceStatus.ACTIVE));
        DeviceEntity laptop = deviceRepository.save(new DeviceEntity("PC-2001", "Engineering Laptop", "Laptop", "Floor 2", bob, DeviceStatus.ACTIVE));
        DeviceEntity vm = deviceRepository.save(new DeviceEntity("VM-3001", "DB Server VM", "VirtualMachine", "Data Center", sam, DeviceStatus.ACTIVE));

        TicketEntity ticketOne = new TicketEntity(alice, printer, "Printer not responding", "The office printer is offline and cannot print documents.", Priority.HIGH);
        ticketOne.setAssignedTechnician(jenny);
        ticketOne.setStatus(TicketStatus.ASSIGNED);
        ticketOne.setTicketNumber("T-1001");

        TicketEntity ticketTwo = new TicketEntity(bob, laptop, "Laptop keeps freezing", "The laptop freezes when opening the internal portal.", Priority.MEDIUM);
        ticketTwo.setAssignedTechnician(marco);
        ticketTwo.setStatus(TicketStatus.IN_PROGRESS);
        ticketTwo.setTicketNumber("T-1002");

        TicketEntity ticketThree = new TicketEntity(alice, vm, "Database service unstable", "The internal DB VM is intermittently unavailable.", Priority.URGENT);
        ticketThree.setAssignedTechnician(jenny);
        ticketThree.setStatus(TicketStatus.RESOLVED);
        ticketThree.setResolutionNote("Restarted database service and verified connectivity.");
        ticketThree.setTicketNumber("T-1003");

        ticketRepository.saveAll(List.of(ticketOne, ticketTwo, ticketThree));
    }
}
