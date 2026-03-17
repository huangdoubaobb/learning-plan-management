package com.example.learningplan.repository;

import com.example.learningplan.entity.Role;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    List<Role> findByCreatedAtBetweenOrderByIdAsc(LocalDateTime start, LocalDateTime end);
}
