package com.valeerp.dispatch;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DispatchGuideSearch {

    private DispatchGuideSearch() {
    }

    static Specification<DispatchGuide> matching(DispatchGuideStatus status,
                                                  DeliveryAssignmentStatus assignment,
                                                  String query) {
        return (root, criteria, builder) -> {
            criteria.distinct(false);
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (assignment != null) {
                predicates.add(builder.equal(root.get("assignmentStatus"), assignment));
            }
            if (query != null && !query.isBlank()) {
                String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                Expression<String> series = root.get("series");
                Expression<String> number = root.get("number");
                Expression<String> documentNumber = builder.lower(builder.concat(
                        builder.concat(builder.coalesce(series, ""), "-"),
                        builder.coalesce(number, "")
                ));
                Expression<String> carrierName = builder.lower(builder.coalesce(root.<String>get("carrierName"), ""));
                Expression<String> carrierCode = builder.lower(builder.coalesce(root.<String>get("carrierCode"), ""));
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("recipientName")), like),
                        builder.like(builder.lower(root.get("senderName")), like),
                        builder.like(documentNumber, like),
                        builder.like(carrierName, like),
                        builder.like(carrierCode, like)
                ));
            }
            if (predicates.isEmpty()) {
                return builder.conjunction();
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
