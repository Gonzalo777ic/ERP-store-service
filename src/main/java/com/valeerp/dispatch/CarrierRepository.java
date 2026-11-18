package com.valeerp.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {

    Optional<Carrier> findByCode(String code);

    Optional<Carrier> findByIdentitySubject(String identitySubject);

    List<Carrier> findByLastFixAtGreaterThanEqual(Instant threshold);
}
