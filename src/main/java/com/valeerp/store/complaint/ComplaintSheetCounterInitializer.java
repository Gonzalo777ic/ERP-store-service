package com.valeerp.store.complaint;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ComplaintSheetCounterInitializer implements ApplicationRunner {

    private final ComplaintSheetCounterRepository counters;

    public ComplaintSheetCounterInitializer(ComplaintSheetCounterRepository counters) {
        this.counters = counters;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!counters.existsById(ComplaintSheetCounter.ID)) {
            counters.save(ComplaintSheetCounter.start());
        }
    }
}
