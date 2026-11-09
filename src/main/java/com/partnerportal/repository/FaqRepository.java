package com.partnerportal.repository;

import com.partnerportal.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findAllByOrderByCategoryAscSortOrderAscIdAsc();

    List<Faq> findByCategoryOrderBySortOrderAscIdAsc(String category);
}
