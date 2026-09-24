package com.example.itsupportticket.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.itsupportticket.domain.model.TicketEntity;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
}
