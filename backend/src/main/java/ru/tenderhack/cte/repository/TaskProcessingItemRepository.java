package ru.tenderhack.cte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tenderhack.cte.entity.TaskProcessingItemEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskProcessingItemRepository extends JpaRepository<TaskProcessingItemEntity, UUID> {

    List<TaskProcessingItemEntity> findByTaskId(UUID taskId);

    @Query("SELECT t FROM TaskProcessingItemEntity t WHERE t.taskId = :taskId")
    List<TaskProcessingItemEntity> findAllByTaskId(@Param("taskId") UUID taskId);

    long countByTaskId(UUID taskId);

    void deleteByTaskId(UUID taskId);
}

