package ru.tenderhack.cte.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.tenderhack.cte.dto.TaskStatus;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class GroupingService {

    private final NotificationService notificationService;
    // private final LlmService llmService; // Ваш сервис LLM
    // private final CteRepository cteRepository; // Репозиторий

    /**
     * Метод запускает тяжелую задачу.
     * Возвращает void, так как работает в фоне.
     */
    @Async
    public void startGroupingTask(UUID taskId, String userQuery) {
        try {
            // 1. Уведомляем, что начали (если нужно, или фронт и так знает)
            notificationService.notifyTaskStatus(taskId, TaskStatus.RUNNING, "Начинаем анализ товаров...");

            // Эмуляция работы LLM (задержка 5 секунд)
            Thread.sleep(2000);
            notificationService.notifyTaskStatus(taskId, TaskStatus.RUNNING, "Генерация эмбеддингов...");

            Thread.sleep(3000);

            // ТУТ ВАША ЛОГИКА ГРУППИРОВКИ
            // var result = llmService.group(userQuery);
            // cteRepository.save(result);

            // 2. Уведомляем об успехе
            notificationService.notifyTaskStatus(taskId, TaskStatus.COMPLETED, "Группировка завершена успешно!");

        } catch (Exception e) {
            // 3. Уведомляем об ошибке
            notificationService.notifyTaskStatus(taskId, TaskStatus.ERROR, "Ошибка при группировке: " + e.getMessage());
        }
    }
}
