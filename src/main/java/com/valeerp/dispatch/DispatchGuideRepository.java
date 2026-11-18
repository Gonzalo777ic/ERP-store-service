package com.valeerp.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DispatchGuideRepository extends JpaRepository<DispatchGuide, Long>, JpaSpecificationExecutor<DispatchGuide> {

    @Query("select max(g.number) from DispatchGuide g where g.series = :series")
    Optional<String> findMaxNumber(@Param("series") String series);
}
