package com.partnerportal.service;

import java.util.List;

/** 화면 선택지. 필요하면 여기서 항목을 바꾸면 된다. */
public final class Categories {

    public static final List<String> HOTLINE = List.of(
            "불공정거래", "금품·향응 요구", "부당한 업무지시", "정보보호 위반", "기타");

    public static final List<String> CONSULT = List.of(
            "신규 입점", "제품·서비스 제안", "공동 마케팅", "기술 제휴", "기타");

    public static final List<String> FAQ = List.of(
            "회원가입", "로그인", "입점상담", "핫라인", "기타");

    private Categories() {
    }
}
