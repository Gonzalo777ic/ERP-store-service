package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;

public record OrderRef(OrderRefKind kind, Long id) {

    public OrderRef {
        if (kind == null || id == null) {
            throw new DomainException("El pedido de la entrega es obligatorio");
        }
    }
}
