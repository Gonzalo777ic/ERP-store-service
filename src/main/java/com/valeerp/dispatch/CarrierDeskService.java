package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CarrierDeskService implements CarrierDesk {

    private final CarrierRepository carriers;
    private final CarrierRouteRepository routes;
    private final OrderDeparturePort departures;
    private final OrderDepartureRepository departureRows;
    private final TimeProvider time;

    public CarrierDeskService(CarrierRepository carriers,
                              CarrierRouteRepository routes,
                              OrderDeparturePort departures,
                              OrderDepartureRepository departureRows,
                              TimeProvider time) {
        this.carriers = carriers;
        this.routes = routes;
        this.departures = departures;
        this.departureRows = departureRows;
        this.time = time;
    }

    @Override
    @Transactional
    public DispatchViews.RouteView accept(AssignmentNotice notice) {
        return routes.findByAssignmentRef(notice.assignmentRef())
                .map(DispatchViews.RouteView::from)
                .orElseGet(() -> open(notice));
    }

    private DispatchViews.RouteView open(AssignmentNotice notice) {
        Carrier carrier = carriers.findById(notice.carrierId())
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE,
                        "Transportista no encontrado"));
        OrderDeparture stored = locate(notice);
        GeoPoint supplied = pointOf(notice.latitude(), notice.longitude());
        GeoPoint destination = supplied != null ? supplied : stored == null ? null : stored.point();
        String label = notice.destinationLabel() != null
                ? notice.destinationLabel()
                : stored == null ? null : stored.label();
        if (supplied != null) {
            remember(notice.orderKind(), notice.orderId(), label, supplied);
        }
        CarrierRoute route = CarrierRoute.open(
                notice.assignmentRef(),
                notice.orderKind(),
                notice.orderId(),
                carrier.getId(),
                notice.scope(),
                notice.summary(),
                label,
                destination,
                time.now());
        try {
            return DispatchViews.RouteView.from(routes.saveAndFlush(route));
        } catch (DataIntegrityViolationException ex) {
            return routes.findByAssignmentRef(notice.assignmentRef())
                    .map(DispatchViews.RouteView::from)
                    .orElseThrow(() -> new DomainException("La asignación ya fue abierta"));
        }
    }

    private void remember(OrderRefKind kind, Long orderId, String label, GeoPoint point) {
        var when = time.now();
        departureRows.findByOrderKindAndOrderId(kind, orderId).ifPresentOrElse(row -> {
            row.relocate(label, point, when);
            departureRows.save(row);
        }, () -> departureRows.save(OrderDeparturePoint.place(kind, orderId, label, point, when)));
    }

    private OrderDeparture locate(AssignmentNotice notice) {
        if (notice.orderKind() == OrderRefKind.DISPATCH_GUIDE) {
            return null;
        }
        try {
            return departures.load(new OrderRef(notice.orderKind(), notice.orderId()));
        } catch (DomainException ex) {
            if (DomainExceptionHandler.NOT_FOUND_CODE.equals(ex.code())) {
                return null;
            }
            throw ex;
        }
    }

    private static GeoPoint pointOf(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null && longitude == null) {
            return null;
        }
        return GeoPoint.of(latitude, longitude);
    }
}
