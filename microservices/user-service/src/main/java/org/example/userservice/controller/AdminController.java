package org.example.userservice.controller;

import org.example.userservice.dto.ChangeRoleRequest;
import org.example.userservice.entity.AuditLog;
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

    @GetMapping("/users/all")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

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

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return adminService.getUserById(id);
    }

    @PatchMapping("/users/{id}/role")
    public void changeRole(@PathVariable Long id,
                           @RequestBody ChangeRoleRequest request,
                           @AuthenticationPrincipal UserDetails adminPrincipal) {
        String actorEmail = adminPrincipal != null ? adminPrincipal.getUsername() : null;
        adminService.changeRole(id, request.getRole(), actorEmail);
    }

    @PostMapping("/users/{id}/block")
    public void blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
    }

    @PostMapping("/users/{id}/unblock")
    public void unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
    }

    @GetMapping("/audit")
    public Page<AuditLog> getAudit(@RequestParam(required = false) Long userId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return adminService.getAuditLogs(userId, pageable);
    }
}