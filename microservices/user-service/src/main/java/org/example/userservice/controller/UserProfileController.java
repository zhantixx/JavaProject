package org.example.userservice.controller;

import org.example.userservice.dto.UpdateMeRequest;
import org.example.userservice.dto.UserMeResponse;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserRepository userRepository;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.ok(new UserMeResponse(1L, "test@example.com", "Test User", "USER"));
        }
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(new UserMeResponse(user.getId(), user.getEmail(), user.getName(), user.getRole()));
    }

    @PatchMapping
    public ResponseEntity<UserMeResponse> update(@AuthenticationPrincipal UserDetails principal,
                                                 @RequestBody UpdateMeRequest request) {
        if (principal == null) {
            return ResponseEntity.ok(new UserMeResponse(1L, "test@example.com", request.getName() != null ? request.getName() : "Test User", "USER"));
        }
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        userRepository.save(user);
        return ResponseEntity.ok(new UserMeResponse(user.getId(), user.getEmail(), user.getName(), user.getRole()));
    }

}
