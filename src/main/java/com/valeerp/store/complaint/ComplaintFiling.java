package com.valeerp.store.complaint;

import com.valeerp.kernel.Money;
import com.valeerp.partner.IdentityDocument;

/**
 * Datos ya interpretados con los que se abre una hoja. No es el cuerpo HTTP.
 */
record ComplaintFiling(
        Long customerId,
        String fullName,
        IdentityDocument document,
        String address,
        String phone,
        String email,
        boolean minor,
        String guardianName,
        IdentityDocument guardianDocument,
        ComplaintSubjectKind subjectKind,
        String subjectDescription,
        Money claimedAmount,
        String orderReference,
        ComplaintKind kind,
        String detail,
        String consumerRequest
) {
}
