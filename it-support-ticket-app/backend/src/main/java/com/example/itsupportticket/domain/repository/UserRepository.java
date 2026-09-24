package com.example.itsupportticket.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.itsupportticket.domain.model.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
