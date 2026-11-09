package com.partnerportal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 입점상담 신청. 비회원도 신청 가능하며, 접수번호 + 조회 비밀번호로 상담 현황을 확인한다.
 */
@Entity
@Table(name = "consults")
@Getter
@Setter
@NoArgsConstructor
public class Consult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String receiptNo;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, length = 10)
    private String bizNo;

    @Column(nullable = false, length = 50)
    private String managerName;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(length = 300)
    private String homepage;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, length = 100)
    private String lookupPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.RECEIVED;

    @Column(columnDefinition = "text")
    private String answer;

    private LocalDateTime answeredAt;
}
