package org.example.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.ExportDataDto;
import org.example.userservice.dto.TaskExportDto;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GdprExportService {

    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${task.service.url:http://localhost:8082}")
    private String taskServiceUrl;

    public byte[] exportUserData(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<TaskExportDto> tasks = fetchUserTasks(userId);

        ExportDataDto exportData = new ExportDataDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                tasks
        );

        return objectMapper.writeValueAsBytes(exportData);
    }

    private List<TaskExportDto> fetchUserTasks(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri(taskServiceUrl + "/api/tasks?userId=" + userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TaskExportDto>>() {})
                .block();
    }
}