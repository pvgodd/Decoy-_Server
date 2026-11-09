package com.partnerportal.repository;

import com.partnerportal.domain.Hotline;
import com.partnerportal.domain.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotlineRepository extends JpaRepository<Hotline, Long> {

    Optional<Hotline> findByReceiptNo(String receiptNo);

    boolean existsByReceiptNo(String receiptNo);

    long countByStatusNot(InquiryStatus status);
}
