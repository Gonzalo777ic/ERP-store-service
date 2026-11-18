package com.valeerp.dispatch;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Unicidad del código y de la identidad solo entre fichas vigentes.
 */
@Component
public class DispatchSchema {

    private final JdbcTemplate jdbc;

    public DispatchSchema(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void partialUniqueCarriers() {
        jdbc.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS carriers_code_active
                ON carriers (code)
                WHERE deleted_at IS NULL
                """);
        jdbc.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS carriers_identity_active
                ON carriers (identity_subject)
                WHERE deleted_at IS NULL
                """);
    }
}
