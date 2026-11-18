package com.valeerp.dispatch;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/dispatch")
public class CarrierRouteController {

    private final CarrierInboxService inbox;

    public CarrierRouteController(CarrierInboxService inbox) {
        this.inbox = inbox;
    }

    @GetMapping("/tracking-policy")
    public DispatchViews.TrackingPolicyView policy() {
        return inbox.policy();
    }

    @GetMapping("/inbox")
    public DispatchViews.InboxView inbox(@AuthenticationPrincipal Jwt jwt) {
        return inbox.inbox(jwt.getSubject());
    }

    @PostMapping("/routes/{id:\\d+}/start")
    public DispatchViews.RouteView start(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return inbox.start(id, jwt.getSubject());
    }

    @PostMapping("/routes/{id:\\d+}/deliver")
    public DispatchViews.RouteView deliver(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return inbox.deliver(id, jwt.getSubject());
    }

    @PostMapping("/routes/{id:\\d+}/locations")
    public DispatchViews.RouteView recordLocation(@PathVariable Long id,
                                                  @RequestBody LocationFix request,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return inbox.recordLocation(id, request.latitude(), request.longitude(), jwt.getSubject());
    }

    public record LocationFix(BigDecimal latitude, BigDecimal longitude) {
    }
}
