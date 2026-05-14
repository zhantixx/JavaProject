package org.example.notificationservice.controller;

import jakarta.persistence.EntityNotFoundException;
import org.example.notificationservice.entity.EmailTemplate;
import org.example.notificationservice.service.EmailTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/email-templates")
public class AdminEmailTemplateController {

    private final EmailTemplateService service;

    public AdminEmailTemplateController(EmailTemplateService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmailTemplate> list() {
        return service.list();
    }

    @GetMapping("{code}")
    public EmailTemplate get(@PathVariable String code) {
        return service.getByCode(code);
    }

    @PutMapping("{code}")
    public ResponseEntity<EmailTemplate> upsert(@PathVariable String code,
                                                @RequestBody EmailTemplateRequest req) {
        EmailTemplate saved = service.upsert(code, req.subject(), req.body(), req.html());
        return ResponseEntity.status(HttpStatus.OK).body(saved);
    }

    @DeleteMapping("{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> notFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    public static record EmailTemplateRequest(String subject, String body, Boolean html) {}
}
