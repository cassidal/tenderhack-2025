package ru.tenderhack.cte.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tenderhack.cte.entity.CteEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CteRepository extends JpaRepository<CteEntity, UUID> {

    Page<CteEntity> findByTaskId(UUID taskId, Pageable pageable);

    List<CteEntity> findByTaskId(UUID taskId);

    @Query("SELECT c FROM CteEntity c WHERE c.taskId = :taskId")
    List<CteEntity> findAllByTaskId(@Param("taskId") UUID taskId);

    void deleteByTaskId(UUID taskId);

    long countByTaskId(UUID taskId);
}

