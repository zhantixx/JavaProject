package org.example.userservice.service;

import org.example.userservice.entity.User;
import org.example.userservice.exception.RoleChangeNotAllowedException;
import org.example.userservice.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminService {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN");

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void changeRole(Long targetUserId, String newRoleRaw, String actorEmail) {
        String newRole = normalizeRole(newRoleRaw);
        validateRole(newRole);

        User target = userRepository.findById(targetUserId).orElseThrow();

        // Ничего менять не нужно
        if (newRole.equalsIgnoreCase(target.getRole())) {
            return;
        }

        // ABAC: запрещаем понижать последнего администратора
        if ("ADMIN".equalsIgnoreCase(target.getRole()) && "USER".equalsIgnoreCase(newRole)) {
            long adminsCount = userRepository.findAll().stream()
                    .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                    .count();
            if (adminsCount <= 1) {
                throw new RoleChangeNotAllowedException("Нельзя понизить роль единственного администратора");
            }
        }

        // Доп. защита: запретить администратору «разжаловать» самого себя, если это приведёт к отсутствию админов
        if (actorEmail != null && actorEmail.equalsIgnoreCase(target.getEmail())
                && "ADMIN".equalsIgnoreCase(target.getRole())
                && "USER".equalsIgnoreCase(newRole)) {
            long adminsCount = userRepository.findAll().stream()
                    .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                    .count();
            if (adminsCount <= 1) {
                throw new RoleChangeNotAllowedException("Нельзя понизить самого себя, если вы последний администратор");
            }
        }

        target.setRole(newRole);
        userRepository.save(target);
    }

    private void validateRole(String role) {
        if (role == null || !ALLOWED_ROLES.contains(role.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Недопустимая роль: " + role);
        }
    }

    private String normalizeRole(String role) {
        return role == null ? null : role.trim().toUpperCase(Locale.ROOT);
    }
}
