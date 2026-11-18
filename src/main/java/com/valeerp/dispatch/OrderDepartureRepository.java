package com.valeerp.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderDepartureRepository extends JpaRepository<OrderDeparturePoint, Long> {

    Optional<OrderDeparturePoint> findByOrderKindAndOrderId(OrderRefKind orderKind, Long orderId);
}
