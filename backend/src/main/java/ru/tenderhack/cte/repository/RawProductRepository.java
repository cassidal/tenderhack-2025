package ru.tenderhack.cte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tenderhack.cte.entity.RawProductEntity;

@Repository
public interface RawProductRepository extends JpaRepository<RawProductEntity, Long> {
}

