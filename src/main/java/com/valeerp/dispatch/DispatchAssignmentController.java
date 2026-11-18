package com.valeerp.dispatch;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Traduce el aviso de asignación al escritorio del transportista.
 * Quien asigna la guía llama aquí; este controlador no persiste la guía.
 */
@RestController
@RequestMapping("/api/dispatch/assignments")
public class DispatchAssignmentController {

    private final CarrierDesk desk;

    public DispatchAssignmentController(CarrierDesk desk) {
        this.desk = desk;
    }

    @PostMapping
    public DispatchViews.RouteView accept(@RequestBody AcceptAssignment request) {
        return desk.accept(request.notice());
    }

    public record AcceptAssignment(
            String assignmentRef,
            String orderKind,
            Long orderId,
            Long carrierId,
            String scope,
            String summary,
            String destinationLabel,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        AssignmentNotice notice() {
            return new AssignmentNotice(
                    assignmentRef,
                    OrderRefKind.parse(orderKind),
                    orderId,
                    carrierId,
                    DeliveryScope.parse(scope),
                    summary,
                    destinationLabel,
                    latitude,
                    longitude);
        }
    }
}
