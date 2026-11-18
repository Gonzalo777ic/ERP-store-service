package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Línea de la guía: producto y cantidad declarados en el traslado. Sin precio ni stock.
 */
@Entity
@Table(name = "dispatch_guide_lines")
public class DispatchGuideLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispatch_guide_id", nullable = false)
    private DispatchGuide guide;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = DispatchGuidePolicy.PRODUCT_NAME_MAX_LENGTH)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    protected DispatchGuideLine() {
    }

    static DispatchGuideLine create(DispatchGuide guide, Long productId, String productName, int quantity) {
        if (guide == null) {
            throw new DomainException("La línea exige una guía");
        }
        if (productId != null && productId <= 0) {
            throw new DomainException("El id de producto no es válido");
        }
        if (quantity <= 0) {
            throw new DomainException("La cantidad debe ser mayor que cero");
        }
        DispatchGuideLine line = new DispatchGuideLine();
        line.guide = guide;
        line.productId = productId;
        line.productName = requireName(productName);
        line.quantity = quantity;
        return line;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    private static String requireName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new DomainException("El producto de la línea es obligatorio");
        }
        String trimmed = productName.trim();
        if (trimmed.length() > DispatchGuidePolicy.PRODUCT_NAME_MAX_LENGTH) {
            throw new DomainException("El nombre del producto supera la longitud permitida");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DispatchGuideLine line)) {
            return false;
        }
        return id != null && id.equals(line.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
