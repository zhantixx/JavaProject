package org.example.notificationservice.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class DeadlineScheduler {

    @Scheduled(cron = "0 */5 * * * *")
    public void checkDeadlines() {
        log.info("Checking deadlines... (Kafka events will be processed here in future)");
    }
}