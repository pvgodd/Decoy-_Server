package com.partnerportal.security;

import com.partnerportal.domain.Member;
import com.partnerportal.domain.MemberStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class PortalUserDetails extends User {

    private final Long memberId;
    private final String companyName;
    private final MemberStatus status;

    public PortalUserDetails(Member member) {
        super(member.getLoginId(),
                member.getPassword(),
                member.getStatus() == MemberStatus.ACTIVE,
                true,
                true,
                member.getStatus() != MemberStatus.LOCKED,
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));
        this.memberId = member.getId();
        this.companyName = member.getCompanyName();
        this.status = member.getStatus();
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public MemberStatus getStatus() {
        return status;
    }
}
