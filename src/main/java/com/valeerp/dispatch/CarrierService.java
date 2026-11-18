package com.valeerp.dispatch;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class CarrierService {

    private final CarrierRepository carriers;
    private final TimeProvider time;

    public CarrierService(CarrierRepository carriers, TimeProvider time) {
        this.carriers = carriers;
        this.time = time;
    }

    public List<DispatchViews.CarrierView> list() {
        return carriers.findAll().stream()
                .sorted(Comparator.comparing(Carrier::getDisplayName))
                .map(DispatchViews.CarrierView::from)
                .toList();
    }

    public Carrier require(Long id) {
        return carriers.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Transportista no encontrado"));
    }

    public Carrier requireSession(String identitySubject) {
        if (identitySubject == null || identitySubject.isBlank()) {
            throw new DomainException("La sesión del transportista es obligatoria");
        }
        return carriers.findByIdentitySubject(identitySubject.trim())
                .orElseThrow(() -> new DomainException(
                        DomainExceptionHandler.NOT_FOUND_CODE,
                        "No hay ficha de transportista para esta sesión"));
    }

    @Transactional
    public DispatchViews.CarrierView register(String code, String displayName, String identitySubject, String phone) {
        String normalized = Carrier.normalizeCode(code);
        carriers.findByCode(normalized).ifPresent(existing -> {
            throw new DomainException("Ya existe un transportista vigente con código " + normalized);
        });
        Carrier draft = Carrier.register(code, displayName, identitySubject, phone);
        carriers.findByIdentitySubject(draft.getIdentitySubject()).ifPresent(existing -> {
            throw new DomainException("Esa identidad ya tiene una ficha de transportista vigente");
        });
        try {
            return DispatchViews.CarrierView.from(carriers.saveAndFlush(draft));
        } catch (DataIntegrityViolationException ex) {
            throw new DomainException("Ya existe un transportista vigente con ese código o identidad");
        }
    }

    @Transactional
    public void retire(Long id) {
        Carrier carrier = require(id);
        carrier.archive(Actors.currentActor(), time.now());
        carriers.save(carrier);
    }
}
