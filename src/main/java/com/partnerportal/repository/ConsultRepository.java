package com.partnerportal.repository;

import com.partnerportal.domain.Consult;
import com.partnerportal.domain.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultRepository extends JpaRepository<Consult, Long> {

    Optional<Consult> findByReceiptNo(String receiptNo);

    boolean existsByReceiptNo(String receiptNo);

    long countByStatusNot(InquiryStatus status);
}
