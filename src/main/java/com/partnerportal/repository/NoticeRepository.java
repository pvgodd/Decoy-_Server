package com.partnerportal.repository;

import com.partnerportal.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    List<Notice> findTop5ByOrderByPinnedDescCreatedAtDesc();

    @Modifying
    @Query("update Notice n set n.viewCount = n.viewCount + 1 where n.id = :id")
    void increaseViewCount(@Param("id") Long id);
}
