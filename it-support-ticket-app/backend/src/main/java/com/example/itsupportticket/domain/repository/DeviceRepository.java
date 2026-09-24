package com.example.itsupportticket.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.itsupportticket.domain.model.DeviceEntity;

public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
}
