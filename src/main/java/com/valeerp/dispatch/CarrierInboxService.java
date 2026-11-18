package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarrierInboxService {

    private final CarrierService carriers;
    private final CarrierRepository carrierRepository;
    private final CarrierRouteRepository routes;
    private final RouteLocationRepository locations;
    private final DispatchGuideRepository guides;
    private final CarrierDesk desk;
    private final DispatchPolicy policy;
    private final TimeProvider time;

    public CarrierInboxService(CarrierService carriers,
                               CarrierRepository carrierRepository,
                               CarrierRouteRepository routes,
                               RouteLocationRepository locations,
                               DispatchGuideRepository guides,
                               CarrierDesk desk,
                               DispatchPolicy policy,
                               TimeProvider time) {
        this.carriers = carriers;
        this.carrierRepository = carrierRepository;
        this.routes = routes;
        this.locations = locations;
        this.guides = guides;
        this.desk = desk;
        this.policy = policy;
        this.time = time;
    }

    public DispatchViews.TrackingPolicyView policy() {
        return new DispatchViews.TrackingPolicyView(
                policy.locationInterval().toSeconds(),
                policy.locationFreshness().toSeconds());
    }

    @Transactional
    public DispatchViews.InboxView inbox(String identitySubject) {
        Carrier carrier = carriers.requireSession(identitySubject);
        guides.findAll(DispatchGuideSearch.matching(
                        DispatchGuideStatus.ISSUED,
                        DeliveryAssignmentStatus.ASSIGNED,
                        null))
                .stream()
                .filter(guide -> carrier.getId().equals(guide.getCarrierId())
                        && guide.getAssignmentRef() != null
                        && guide.getDeliveryScope() != null)
                .forEach(guide -> desk.accept(AssignmentNotice.fromGuide(guide)));
        List<DispatchViews.RouteView> rows = routes.findByCarrierIdOrderByAssignedAtDesc(carrier.getId()).stream()
                .map(DispatchViews.RouteView::from)
                .toList();
        return new DispatchViews.InboxView(carrier.getId(), carrier.getCode(), carrier.getDisplayName(), rows);
    }

    @Transactional
    public DispatchViews.RouteView start(Long routeId, String identitySubject) {
        CarrierRoute route = owned(routeId, identitySubject);
        route.start(time.now());
        return DispatchViews.RouteView.from(routes.save(route));
    }

    @Transactional
    public DispatchViews.RouteView deliver(Long routeId, String identitySubject) {
        CarrierRoute route = owned(routeId, identitySubject);
        route.deliver(time.now());
        return DispatchViews.RouteView.from(routes.save(route));
    }

    @Transactional
    public DispatchViews.RouteView recordLocation(Long routeId, BigDecimal latitude, BigDecimal longitude,
                                                  String identitySubject) {
        CarrierRoute route = owned(routeId, identitySubject);
        GeoPoint point = GeoPoint.of(latitude, longitude);
        RouteLocation fix = route.acceptFix(point, time.now(), policy.locationInterval());
        routes.save(route);
        locations.save(fix);
        Carrier carrier = carriers.require(route.getCarrierId());
        carrier.noteFix(point, fix.getRecordedAt());
        carrierRepository.save(carrier);
        return DispatchViews.RouteView.from(route);
    }

    private CarrierRoute owned(Long routeId, String identitySubject) {
        CarrierRoute route = routes.findById(routeId)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Recorrido no encontrado"));
        Carrier carrier = carriers.require(route.getCarrierId());
        if (!carrier.sessionMatches(identitySubject)) {
            throw new DomainException("La sesión no corresponde al transportista de esta entrega");
        }
        return route;
    }
}
