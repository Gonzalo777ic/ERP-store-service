package com.valeerp.dispatch;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/dispatch")
public class NearbyCarriersController {

    private final NearestCarriersService nearest;

    public NearbyCarriersController(NearestCarriersService nearest) {
        this.nearest = nearest;
    }

    @GetMapping("/carriers/nearby")
    public DispatchViews.NearbyResponse nearby(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String orderKind,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude) {
        return nearest.nearest(orderId, orderKind, latitude, longitude);
    }

    @PutMapping("/orders/{orderId:\\d+}/departure")
    public DispatchViews.DepartureView remember(@PathVariable Long orderId, @RequestBody DepartureRequest request) {
        return nearest.remember(orderId, request.orderKind(), request.latitude(), request.longitude(), request.label());
    }

    public record DepartureRequest(String orderKind, BigDecimal latitude, BigDecimal longitude, String label) {
    }
}
