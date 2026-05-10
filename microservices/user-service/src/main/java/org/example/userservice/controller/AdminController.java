package org.example.userservice.controller;

import org.example.userservice.dto.ChangeRoleRequest;
import org.example.userservice.entity.User;
import org.example.userservice.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Список пользователей (без пагинации)
    @GetMapping("/users/all")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    // Список пользователей с пагинацией и сортировкой
    @GetMapping("/users")
    public Page<User> getUsers(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "id,asc") String sort) {
        String[] parts = sort.split(",");
        String sortField = parts[0];
        Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return adminService.getUsers(pageable);
    }

    // Один пользователь по id
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return adminService.getUserById(id);
    }

    // Смена роли через JSON-тело, с учетом ABAC-правил
    @PatchMapping("/users/{id}/role")
    public void changeRole(@PathVariable Long id,
                           @RequestBody ChangeRoleRequest request,
                           @AuthenticationPrincipal UserDetails adminPrincipal) {
        String actorEmail = adminPrincipal != null ? adminPrincipal.getUsername() : null;
        adminService.changeRole(id, request.getRole(), actorEmail);
    }
}