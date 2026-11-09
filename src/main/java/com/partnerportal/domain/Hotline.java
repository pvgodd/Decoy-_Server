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
 * 핫라인(윤리·불공정 제보). 익명 접수가 가능하며, 접수번호 + 조회 비밀번호로 처리 결과를 확인한다.
 */
@Entity
@Table(name = "hotlines")
@Getter
@Setter
@NoArgsConstructor
public class Hotline extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String receiptNo;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    /** 비어 있으면 익명 */
    @Column(length = 50)
    private String reporterName;

    @Column(length = 120)
    private String contact;

    /** 조회용 비밀번호 (BCrypt) */
    @Column(nullable = false, length = 100)
    private String lookupPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.RECEIVED;

    @Column(columnDefinition = "text")
    private String answer;

    private LocalDateTime answeredAt;
}
