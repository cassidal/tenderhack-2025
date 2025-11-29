package ru.tenderhack.cte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tenderhack.cte.entity.GroupingTaskEntity;
import ru.tenderhack.cte.entity.Status;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupingTaskRepository extends JpaRepository<GroupingTaskEntity, UUID> {

    List<GroupingTaskEntity> findByStatus(Status status);

    List<GroupingTaskEntity> findByStatusIn(List<Status> statuses);
}

