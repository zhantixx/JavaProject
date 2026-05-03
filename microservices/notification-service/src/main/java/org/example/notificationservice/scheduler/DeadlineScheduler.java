package org.example.notificationservice.scheduler;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

@Component
@EnableScheduling
public class DeadlineScheduler {

    private static final Logger LOGGER = Logger.getLogger(DeadlineScheduler.class.getName());

    @Scheduled(cron = "0 */5 * * * *")
    public void checkDeadlines() {
        LOGGER.info("Checking deadlines... (Kafka events will be processed here in future)");
    }
}