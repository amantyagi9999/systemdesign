package com.demo.repository;

import com.demo.entity.IdRangeCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IdRangeCounterRepository extends JpaRepository<IdRangeCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from IdRangeCounter c where c.id = 1")
    Optional<IdRangeCounter> findIdForUpdate();
}
