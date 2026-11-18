package com.valeerp.store.complaint;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ComplaintSheetCounterRepository extends JpaRepository<ComplaintSheetCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select counter from ComplaintSheetCounter counter where counter.id = :id")
    Optional<ComplaintSheetCounter> lockById(@Param("id") Long id);
}
