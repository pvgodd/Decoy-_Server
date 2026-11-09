package com.partnerportal.repository;

import com.partnerportal.domain.Member;
import com.partnerportal.domain.MemberRole;
import com.partnerportal.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByRole(MemberRole role);

    List<Member> findByCompanyNameAndEmailIgnoreCase(String companyName, String email);

    Optional<Member> findByLoginIdAndEmailIgnoreCase(String loginId, String email);

    Page<Member> findByStatus(MemberStatus status, Pageable pageable);

    long countByStatus(MemberStatus status);
}
