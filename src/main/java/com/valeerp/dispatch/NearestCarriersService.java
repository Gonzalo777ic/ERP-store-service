package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class NearestCarriersService {

    private final CarrierRepository carriers;
    private final OrderDeparturePort departures;
    private final OrderDepartureRepository departureRows;
    private final DispatchPolicy policy;
    private final TimeProvider time;

    public NearestCarriersService(CarrierRepository carriers,
                                  OrderDeparturePort departures,
                                  OrderDepartureRepository departureRows,
                                  DispatchPolicy policy,
                                  TimeProvider time) {
        this.carriers = carriers;
        this.departures = departures;
        this.departureRows = departureRows;
        this.policy = policy;
        this.time = time;
    }

    @Transactional(readOnly = true)
    public DispatchViews.NearbyResponse nearest(Long orderId, String orderKind, BigDecimal latitude, BigDecimal longitude) {
        Origin origin = resolve(orderId, orderKind, latitude, longitude);
        Instant now = time.now();
        List<NearbyRanking.Fix> fixes = carriers.findByLastFixAtGreaterThanEqual(now.minus(policy.locationFreshness()))
                .stream()
                .filter(carrier -> carrier.getLastFix() != null && carrier.getLastFixAt() != null)
                .map(carrier -> new NearbyRanking.Fix(
                        carrier.getId(),
                        carrier.getCode(),
                        carrier.getDisplayName(),
                        carrier.getLastFix(),
                        carrier.getLastFixAt()))
                .toList();
        List<DispatchViews.NearbyCarrierView> ranked = NearbyRanking
                .rank(origin.point(), fixes, policy.locationFreshness(), now)
                .stream()
                .map(DispatchViews.NearbyCarrierView::from)
                .toList();
        return new DispatchViews.NearbyResponse(
                new DispatchViews.OriginView(
                        origin.kind() == null ? null : origin.kind().name(),
                        origin.orderId(),
                        origin.label(),
                        origin.point().latitude(),
                        origin.point().longitude()),
                ranked);
    }

    @Transactional
    public DispatchViews.DepartureView remember(Long orderId, String orderKind, BigDecimal latitude,
                                                BigDecimal longitude, String label) {
        OrderRef ref = new OrderRef(OrderRefKind.parse(orderKind), orderId);
        OrderDeparture stored = departures.load(ref);
        GeoPoint point = GeoPoint.of(latitude, longitude);
        String nextLabel = label == null || label.isBlank() ? stored.label() : label.trim();
        Instant when = time.now();
        departureRows.findByOrderKindAndOrderId(ref.kind(), ref.id()).ifPresentOrElse(row -> {
            row.relocate(nextLabel, point, when);
            departureRows.save(row);
        }, () -> departureRows.save(OrderDeparturePoint.place(ref.kind(), ref.id(), nextLabel, point, when)));
        return DispatchViews.DepartureView.from(new OrderDeparture(ref, nextLabel, point));
    }

    private Origin resolve(Long orderId, String orderKind, BigDecimal latitude, BigDecimal longitude) {
        boolean hasPoint = latitude != null || longitude != null;
        if (hasPoint) {
            GeoPoint point = GeoPoint.of(latitude, longitude);
            if (orderId == null) {
                return new Origin(null, null, null, point);
            }
            OrderDeparture stored = departures.load(new OrderRef(OrderRefKind.parse(orderKind), orderId));
            return new Origin(stored.ref().kind(), stored.ref().id(), stored.label(), point);
        }
        if (orderId == null) {
            throw new DomainException("Indica un punto de partida o el pedido");
        }
        OrderDeparture stored = departures.load(new OrderRef(OrderRefKind.parse(orderKind), orderId));
        if (stored.point() == null) {
            throw new DomainException("El pedido no tiene un punto de partida guardado");
        }
        return new Origin(stored.ref().kind(), stored.ref().id(), stored.label(), stored.point());
    }

    private record Origin(OrderRefKind kind, Long orderId, String label, GeoPoint point) {
    }
}
