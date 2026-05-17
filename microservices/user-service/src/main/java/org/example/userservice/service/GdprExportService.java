package org.example.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.dto.TaskExportDto;
import org.example.userservice.dto.UserExportDto;
import org.example.userservice.dto.UserWithTasksExportDto;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GdprExportService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String taskServiceUrl;

    public GdprExportService(UserRepository userRepository,
                             RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             @Value("${task.service.url:http://localhost:8082}") String taskServiceUrl) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.taskServiceUrl = taskServiceUrl;
    }

    public byte[] exportUserData(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<TaskExportDto> tasks = fetchUserTasks(userId);

        UserExportDto userDto = new UserExportDto(user.getId(), user.getEmail(), user.getName());
        UserWithTasksExportDto exportDto = new UserWithTasksExportDto(userDto, tasks);

        return objectMapper.writeValueAsBytes(exportDto);
    }

    private List<TaskExportDto> fetchUserTasks(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", String.valueOf(userId));

        ResponseEntity<List<TaskExportDto>> response = restTemplate.exchange(
                taskServiceUrl + "/api/tasks",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<TaskExportDto>>() {}
        );
        return response.getBody();
    }
}
