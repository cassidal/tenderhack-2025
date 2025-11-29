package ru.tenderhack.cte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.tenderhack.cte.dto.TaskStatusEvent;
import ru.tenderhack.cte.entity.Status;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Отправляет обновление статуса в топик /topic/tasks/{taskId}/status
     */
    public void notifyTaskStatus(UUID taskId, Status status, String message) {
        String destination = "/topic/tasks/" + taskId + "/status";

        TaskStatusEvent event = new TaskStatusEvent(
                taskId,
                status,
                null, // progress можно добавить по желанию
                message,
                Instant.now()
        );

        log.info("Sending WS notification to {}: {}", destination, status);
        messagingTemplate.convertAndSend(destination, event);
    }
}
