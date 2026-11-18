package com.valeerp.dispatch;

import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/dispatch/guides")
public class DispatchGuideController {

    private final DispatchGuideService dispatchGuideService;

    public DispatchGuideController(DispatchGuideService dispatchGuideService) {
        this.dispatchGuideService = dispatchGuideService;
    }

    @GetMapping("/transfer-reasons")
    public List<CatalogItem> transferReasons() {
        return Arrays.stream(TransferReason.values())
                .map(reason -> new CatalogItem(reason.name(), reason.label()))
                .toList();
    }

    @GetMapping("/statuses")
    public List<CatalogItem> statuses() {
        return Arrays.stream(DispatchGuideStatus.values())
                .map(status -> new CatalogItem(status.name(), status.label()))
                .toList();
    }

    @GetMapping("/delivery-scopes")
    public List<CatalogItem> deliveryScopes() {
        return Arrays.stream(DeliveryScope.values())
                .map(scope -> new CatalogItem(scope.name(), scope.label()))
                .toList();
    }

    @GetMapping("/assignment-statuses")
    public List<CatalogItem> assignmentStatuses() {
        return Arrays.stream(DeliveryAssignmentStatus.values())
                .map(status -> new CatalogItem(status.name(), status.label()))
                .toList();
    }

    @GetMapping
    public PageResponse<DispatchGuideResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignmentStatus,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) boolean assignable,
            @PageableDefault(size = DispatchGuidePolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        return dispatchGuideService.list(status, assignmentStatus, q, assignable, pageable);
    }

    @GetMapping("/{id}")
    public DispatchGuideResponse get(@PathVariable Long id) {
        return dispatchGuideService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DispatchGuideResponse create(@RequestBody DispatchGuideRequests.CreateDispatchGuideRequest request) {
        return dispatchGuideService.create(request);
    }

    @PostMapping("/{id}/issue")
    public DispatchGuideResponse issue(@PathVariable Long id) {
        return dispatchGuideService.issue(id);
    }

    @PostMapping("/{id}/void")
    public DispatchGuideResponse voidGuide(@PathVariable Long id,
                                            @RequestBody DispatchGuideRequests.VoidDispatchGuideRequest request) {
        return dispatchGuideService.voidGuide(id, request);
    }

    @PostMapping("/{id}/delivery")
    public DispatchGuideResponse assignDelivery(@PathVariable Long id,
                                                 @RequestBody DispatchGuideRequests.AssignDeliveryRequest request) {
        return dispatchGuideService.assignDelivery(id, request);
    }

    public record CatalogItem(String key, String label) {
    }
}
