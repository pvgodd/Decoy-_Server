package com.partnerportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** 화면에 표시할 포털 이름 */
    private String brand = "파트너 포털";
    /** 운영 회사명 (푸터, 개인정보처리방침) */
    private String company = "우리회사";
    /** 메일 링크 생성용 외부 접속 주소 */
    private String baseUrl = "http://localhost:8080";
    private String mailFrom = "no-reply@example.com";

    private Admin admin = new Admin();
    private Security security = new Security();

    @Getter
    @Setter
    public static class Admin {
        private String loginId = "admin";
        private String password = "";
    }

    @Getter
    @Setter
    public static class Security {
        private int maxLoginFailures = 5;
    }
}
