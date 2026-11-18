package com.valeerp.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarrierRouteRepository extends JpaRepository<CarrierRoute, Long> {

    Optional<CarrierRoute> findByAssignmentRef(String assignmentRef);

    List<CarrierRoute> findByCarrierIdOrderByAssignedAtDesc(Long carrierId);
}
