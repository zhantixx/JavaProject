package org.example.notificationservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.notificationservice.entity.EmailTemplate;
import org.example.notificationservice.repository.EmailTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class EmailTemplateService {

    private final EmailTemplateRepository repository;

    public EmailTemplateService(EmailTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EmailTemplate> list() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public EmailTemplate getByCode(String code) {
        String c = normalizeCode(code);
        return repository.findByCode(c).orElseThrow(EntityNotFoundException::new);
    }

    public EmailTemplate upsert(String code, String subject, String body, Boolean html) {
        String c = normalizeCode(code);
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject is required");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body is required");
        }
        EmailTemplate tpl = repository.findByCode(c).orElseGet(EmailTemplate::new);
        tpl.setCode(c);
        tpl.setSubject(subject.trim());
        tpl.setBody(body);
        tpl.setHtml(Boolean.TRUE.equals(html));
        return repository.save(tpl);
    }

    public void delete(String code) {
        String c = normalizeCode(code);
        EmailTemplate tpl = repository.findByCode(c).orElseThrow(EntityNotFoundException::new);
        repository.delete(tpl);
    }

    private String normalizeCode(String code) {
        String c = Objects.toString(code, "").trim();
        if (c.isEmpty()) throw new IllegalArgumentException("code is required");
        return c.toUpperCase(Locale.ROOT);
    }
}
