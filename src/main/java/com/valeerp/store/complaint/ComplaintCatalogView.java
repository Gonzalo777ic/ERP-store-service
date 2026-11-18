package com.valeerp.store.complaint;

import com.valeerp.partner.DocumentFormatRule;
import com.valeerp.partner.DocumentType;
import com.valeerp.partner.PartnerDocumentPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record ComplaintCatalogView(
        List<CodedOption> kinds,
        List<DocumentOption> documentTypes,
        List<CodedOption> subjectKinds,
        List<CodedOption> statuses
) {

    public record CodedOption(String code, String label, String description) {
    }

    public record DocumentOption(String code, String label, int minLength, int maxLength) {
    }

    public static ComplaintCatalogView from(PartnerDocumentPolicy policy) {
        List<DocumentOption> documents = new ArrayList<>();
        for (DocumentType type : DocumentType.values()) {
            DocumentFormatRule rule = policy.formatPolicy().catalog().require(type);
            documents.add(new DocumentOption(type.name(), type.description(), rule.minLength(), rule.maxLength()));
        }
        return new ComplaintCatalogView(
                Arrays.stream(ComplaintKind.values())
                        .map(kind -> new CodedOption(kind.name(), kind.label(), kind.description()))
                        .toList(),
                List.copyOf(documents),
                Arrays.stream(ComplaintSubjectKind.values())
                        .map(kind -> new CodedOption(kind.name(), kind.label(), kind.description()))
                        .toList(),
                Arrays.stream(ComplaintStatus.values())
                        .map(status -> new CodedOption(status.name(), status.label(), status.description()))
                        .toList()
        );
    }
}
