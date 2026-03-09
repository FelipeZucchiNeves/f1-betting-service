package com.f1betting.repository;

import com.f1betting.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Uses default JpaRepository methods: findById, save, etc.
}
