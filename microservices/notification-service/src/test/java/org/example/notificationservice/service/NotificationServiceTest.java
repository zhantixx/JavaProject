package org.example.notificationservice.service;

import org.example.notificationservice.dto.NotificationRequest;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationEntity;
import org.example.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EmailService emailService;

    @Test
    void sendNotification_shouldMarkEmailAsSent() {
        NotificationService service = new NotificationService(notificationRepository, emailService);
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });
        when(emailService.sendEmail("user@example.com", "Reminder", "Body")).thenReturn(true);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setRecipientEmail("user@example.com");
        request.setType("EMAIL");
        request.setTitle("Reminder");
        request.setContent("Body");

        NotificationResponse response = service.sendNotification(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo("SENT");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getStatus()).isEqualTo(NotificationEntity.NotificationStatus.SENT);
    }
}
