package org.example.taskservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.taskservice.entity.Tag;
import org.example.taskservice.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@Transactional
public class TagService {

    private static final Pattern HEX_COLOR = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<Tag> list(Long userId) {
        requireUserId(userId);
        return tagRepository.findAllByUserIdOrderByNameAsc(userId);
    }

    public Tag create(Long userId, String name, String color) {
        requireUserId(userId);
        String normalizedName = normalizeName(name);
        String normalizedColor = normalizeColor(color);

        if (tagRepository.existsByUserIdAndName(userId, normalizedName)) {
            throw new IllegalStateException("Tag with the same name already exists");
        }

        Tag tag = new Tag(userId, normalizedName, normalizedColor);
        return tagRepository.save(tag);
    }

    @Transactional(readOnly = true)
    public Tag getOwned(Long userId, Long tagId) {
        requireUserId(userId);
        requireTagId(tagId);
        return tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
    }

    public Tag update(Long userId, Long tagId, String name, String color) {
        requireUserId(userId);
        requireTagId(tagId);

        Tag existing = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        if (name != null) {
            String normalizedName = normalizeName(name);
            if (!normalizedName.equals(existing.getName())
                    && tagRepository.existsByUserIdAndName(userId, normalizedName)) {
                throw new IllegalStateException("Tag with the same name already exists");
            }
            existing.setName(normalizedName);
        }
        if (color != null) {
            existing.setColor(normalizeColor(color));
        }

        return tagRepository.save(existing);
    }

    public void delete(Long userId, Long tagId) {
        requireUserId(userId);
        requireTagId(tagId);
        long deleted = tagRepository.deleteByIdAndUserId(tagId, userId);
        if (deleted == 0) {
            throw new EntityNotFoundException("Tag not found");
        }
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    private void requireTagId(Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("tagId is required");
        }
    }

    private String normalizeName(String name) {
        String n = Objects.toString(name, "").trim();
        if (n.isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        if (n.length() > 64) {
            n = n.substring(0, 64);
        }
        return n;
    }

    private String normalizeColor(String color) {
        if (color == null) return null;
        String c = color.trim().toUpperCase();
        if (c.isEmpty()) return null;
        if (!HEX_COLOR.matcher(c).matches()) {
            throw new IllegalArgumentException("color must be hex like #RRGGBB or #RGB");
        }
        return c;
        }
}
