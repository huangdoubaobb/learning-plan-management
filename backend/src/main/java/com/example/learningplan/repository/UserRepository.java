package com.example.learningplan.repository;

import com.example.learningplan.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrPhone(String username, String phone);
    List<User> findByCreatedAtBetweenOrderByIdAsc(LocalDateTime start, LocalDateTime end);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}
