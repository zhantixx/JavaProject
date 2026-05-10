package org.example.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.userservice.dto.AuditLogRequest;
import org.example.userservice.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/log")
    public ResponseEntity<Void> logAudit(@RequestBody AuditLogRequest request,
                                         HttpServletRequest httpRequest) {
        auditService.log(request.getUserId(), request.getAction(), request.getDetails(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}