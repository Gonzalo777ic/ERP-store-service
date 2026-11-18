package com.valeerp.store.complaint;

import com.valeerp.customer.CustomerService;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.partner.IdentityDocument;
import com.valeerp.partner.PartnerDocumentPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComplaintBookService {

    private final ComplaintSheetRepository sheets;
    private final ComplaintSheetCounterRepository counters;
    private final CustomerService customerService;
    private final PartnerDocumentPolicy documentPolicy;
    private final TimeProvider timeProvider;
    private final String currency;

    public ComplaintBookService(ComplaintSheetRepository sheets,
                                ComplaintSheetCounterRepository counters,
                                CustomerService customerService,
                                PartnerDocumentPolicy documentPolicy,
                                TimeProvider timeProvider,
                                @Value("${valeerp.currency}") String currency) {
        this.sheets = sheets;
        this.counters = counters;
        this.customerService = customerService;
        this.documentPolicy = documentPolicy;
        this.timeProvider = timeProvider;
        this.currency = currency;
    }

    public ComplaintCatalogView catalog() {
        return ComplaintCatalogView.from(documentPolicy);
    }

    @Transactional
    public ComplaintSheetView file(ComplaintCommands.File command, String auth0Subject) {
        if (command == null) {
            throw new DomainException("La hoja de reclamación está vacía");
        }
        IdentityDocument document = documentPolicy.require(command.documentType(), command.documentNumber());
        IdentityDocument guardian = null;
        if (command.minor()) {
            guardian = documentPolicy.require(command.guardianDocumentType(), command.guardianDocumentNumber());
        }
        Money amount = ComplaintAmount.optional(command.claimedAmount(), currency);
        ComplaintFiling filing = new ComplaintFiling(
                resolveCustomer(auth0Subject),
                command.fullName(),
                document,
                command.address(),
                command.phone(),
                command.email(),
                command.minor(),
                command.guardianName(),
                guardian,
                ComplaintSubjectKind.require(command.subjectKind()),
                command.subjectDescription(),
                amount,
                command.orderReference(),
                ComplaintKind.require(command.kind()),
                command.detail(),
                command.consumerRequest()
        );
        ComplaintSheet sheet = ComplaintSheet.open(nextNumber(), filing, timeProvider.now());
        return ComplaintSheetView.from(sheets.save(sheet));
    }

    @Transactional(readOnly = true)
    public List<ComplaintSheetView> list() {
        return sheets.findAllByOrderByFiledAtDescIdDesc().stream().map(ComplaintSheetView::from).toList();
    }

    @Transactional(readOnly = true)
    public ComplaintSheetView get(Long id) {
        return ComplaintSheetView.from(require(id));
    }

    @Transactional
    public ComplaintSheetView startAttention(Long id) {
        ComplaintSheet sheet = require(id);
        sheet.startAttention(Actors.currentActor(), timeProvider.now());
        return ComplaintSheetView.from(sheets.save(sheet));
    }

    @Transactional
    public ComplaintSheetView attend(Long id, ComplaintCommands.Response command) {
        ComplaintSheet sheet = require(id);
        String response = command == null ? null : command.response();
        sheet.attend(Actors.currentActor(), timeProvider.now(), response);
        return ComplaintSheetView.from(sheets.save(sheet));
    }

    @Transactional
    public ComplaintSheetView cancel(Long id, ComplaintCommands.Cancellation command) {
        ComplaintSheet sheet = require(id);
        String reason = command == null ? null : command.reason();
        sheet.cancel(Actors.currentActor(), timeProvider.now(), reason);
        return ComplaintSheetView.from(sheets.save(sheet));
    }

    private ComplaintSheet require(Long id) {
        if (id == null) {
            throw new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "La hoja de reclamación no existe");
        }
        return sheets.findById(id)
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE, "La hoja de reclamación no existe"));
    }

    private Long resolveCustomer(String auth0Subject) {
        if (auth0Subject == null || auth0Subject.isBlank()) {
            return null;
        }
        return customerService.findByAuth(auth0Subject.trim()).map(customer -> customer.getId()).orElse(null);
    }

    private ComplaintSheetNumber nextNumber() {
        ComplaintSheetCounter counter = counters.lockById(ComplaintSheetCounter.ID)
                .orElseThrow(() -> new DomainException("El libro de reclamaciones no tiene correlativo"));
        return ComplaintSheetNumber.of(counter.next());
    }
}
