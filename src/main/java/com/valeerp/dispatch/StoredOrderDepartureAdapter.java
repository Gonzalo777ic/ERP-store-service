package com.valeerp.dispatch;

import com.valeerp.customer.Customer;
import com.valeerp.customer.CustomerService;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.order.CustomerOrder;
import com.valeerp.order.CustomerOrderRepository;
import com.valeerp.partner.AddressKind;
import com.valeerp.partner.Partner;
import com.valeerp.partner.PartnerAddress;
import com.valeerp.partner.PartnerService;
import com.valeerp.sales.SaleOrder;
import com.valeerp.sales.SaleOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Único acople con pedido y partner. No lee ni copia una guía de remisión.
 */
@Service
public class StoredOrderDepartureAdapter implements OrderDeparturePort {

    private final CustomerOrderRepository customerOrders;
    private final CustomerService customers;
    private final SaleOrderRepository saleOrders;
    private final PartnerService partners;
    private final OrderDepartureRepository departures;

    public StoredOrderDepartureAdapter(CustomerOrderRepository customerOrders,
                                       CustomerService customers,
                                       SaleOrderRepository saleOrders,
                                       PartnerService partners,
                                       OrderDepartureRepository departures) {
        this.customerOrders = customerOrders;
        this.customers = customers;
        this.saleOrders = saleOrders;
        this.partners = partners;
        this.departures = departures;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDeparture load(OrderRef ref) {
        String address = switch (ref.kind()) {
            case CUSTOMER_ORDER -> customerAddress(ref.id());
            case SALE_ORDER -> saleAddress(ref.id());
            case DISPATCH_GUIDE -> "Guía " + ref.id();
        };
        OrderDeparturePoint saved = departures.findByOrderKindAndOrderId(ref.kind(), ref.id()).orElse(null);
        String label = saved != null && saved.getLabel() != null ? saved.getLabel() : address;
        GeoPoint point = saved == null ? null : saved.getPoint();
        return new OrderDeparture(ref, label, point);
    }

    private String customerAddress(Long orderId) {
        CustomerOrder order = customerOrders.findById(orderId)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Pedido no encontrado"));
        Customer customer = customers.require(order.getCustomerId());
        if (customer.getAddress() == null || customer.getAddress().isBlank()) {
            return "Pedido " + orderId;
        }
        return customer.getAddress().trim();
    }

    private String saleAddress(Long orderId) {
        SaleOrder order = saleOrders.findById(orderId)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Pedido de venta no encontrado"));
        Partner partner = partners.require(order.getPartnerId());
        return partner.activeAddresses().stream()
                .min(Comparator
                        .comparingInt((PartnerAddress address) -> address.getKind() == AddressKind.SHIPPING ? 0 : 1)
                        .thenComparing(address -> !address.isPrimaryAddress()))
                .map(StoredOrderDepartureAdapter::format)
                .orElse("Pedido de venta " + orderId);
    }

    private static String format(PartnerAddress address) {
        return Stream.of(address.getLine(), address.getCity(), address.getRegion())
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(", "));
    }
}
