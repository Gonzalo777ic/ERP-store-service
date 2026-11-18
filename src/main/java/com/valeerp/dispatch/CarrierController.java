package com.valeerp.dispatch;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch/carriers")
public class CarrierController {

    private final CarrierService carriers;

    public CarrierController(CarrierService carriers) {
        this.carriers = carriers;
    }

    @GetMapping
    public List<DispatchViews.CarrierView> list() {
        return carriers.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DispatchViews.CarrierView register(@RequestBody RegisterCarrier request) {
        return carriers.register(request.code(), request.displayName(), request.identitySubject(), request.phone());
    }

    @PostMapping("/{id:\\d+}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        carriers.retire(id);
        return ResponseEntity.noContent().build();
    }

    public record RegisterCarrier(String code, String displayName, String identitySubject, String phone) {
    }
}
