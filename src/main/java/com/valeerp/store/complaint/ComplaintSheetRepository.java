package com.valeerp.store.complaint;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintSheetRepository extends JpaRepository<ComplaintSheet, Long> {

    List<ComplaintSheet> findAllByOrderByFiledAtDescIdDesc();
}
