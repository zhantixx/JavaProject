package org.example.userservice.repository;

import org.example.userservice.entity.User;
import org.example.userservice.utils.EmailHashUtil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailHash(String emailHash);

    default Optional<User> findByEmail(String email) {
        String normalizedEmail = EmailHashUtil.normalize(email);
        return findByEmailHash(EmailHashUtil.sha256(normalizedEmail));
    }

    List<User> findAllByScheduledDeletionAtBefore(LocalDateTime cutoff);
}
