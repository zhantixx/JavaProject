package org.example.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.AuditLogRequest;
import org.example.userservice.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/log")
    public ResponseEntity<Void> logAudit(@RequestBody AuditLogRequest request,
                                         HttpServletRequest httpRequest) {
        auditService.log(request.getUserId(), request.getAction(), request.getDetails(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}