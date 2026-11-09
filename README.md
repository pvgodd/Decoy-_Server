# 파트너 포털 (Partner Portal)

협력사 전용 포털. 로그인·회원가입·아이디 찾기·비밀번호 초기화, 입점상담 신청/조회,
핫라인(윤리·불공정 제보) 접수/조회, 공지사항, FAQ, 관리자 기능을 포함한 Spring Boot 웹 애플리케이션입니다.

> 이 포털은 **범용 템플릿**입니다. 브랜드명·회사명은 모두 환경변수로 설정하며,
> 특정 회사를 사칭하지 않습니다. 본인(또는 자사) 명의로 운영하세요.

## 기술 스택

- Java 17, Spring Boot 3.3 (Web, Thymeleaf, Security, Data JPA, Validation, Mail)
- PostgreSQL 14+
- 서버 렌더링(Thymeleaf), 외부 프런트엔드 프레임워크 없음

## 주요 기능

| 구분 | 기능 |
| --- | --- |
| 인증 | 로그인(아이디 저장), 로그아웃, 로그인 5회 실패 시 계정 잠금 |
| 회원 | 회원가입(승인 대기 → 관리자 승인), 아이디 중복 확인, 내 정보/비밀번호 변경 |
| 계정 찾기 | 아이디 찾기(회사명+이메일, 마스킹 노출), 비밀번호 초기화(메일 링크, 30분 유효) |
| 입점상담 | 비회원 신청, 접수번호+조회 비밀번호로 현황 조회 |
| 핫라인 | 익명 제보, 접수번호+조회 비밀번호로 처리 결과 조회 |
| 게시판 | 공지사항(검색·페이징·조회수), FAQ(분류별) |
| 관리자 | 회원 승인/잠금, 입점상담·핫라인 처리, 공지·FAQ CRUD |

## 보안 설계 메모

- 비밀번호·조회 비밀번호는 모두 BCrypt 해시로 저장
- 비밀번호 초기화 토큰은 원문을 메일로만 전달하고 DB에는 SHA-256 해시만 저장(일회용·만료)
- 계정 상태(잠금·승인대기) 확인은 비밀번호 검증 **이후**에 수행 → 계정 존재·상태 유추 방지
- 아이디 찾기·비밀번호 초기화는 일치 여부를 화면에 직접 노출하지 않음(열거 공격 방지)
- 보안 헤더(CSP, Referrer-Policy) 적용, 세션 쿠키 HttpOnly

---

## 로컬 실행

```bash
# PostgreSQL 준비 (예: 로컬)
sudo -u postgres psql -f deploy/db-setup.sql

# 환경변수
cp .env.example .env    # 값 수정
set -a && . ./.env && set +a

mvn spring-boot:run
# http://localhost:8080  (최초 관리자 계정/비번은 .env 또는 기동 로그 확인)
```

테스트는 H2 인메모리로 돌아갑니다(별도 DB 불필요):

```bash
mvn test
```

---

## 서버 설치 (Ubuntu 22.04 기준, 직접 설치 방식)

### 1) 패키지 설치

```bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless postgresql maven git
```

### 2) DB 생성

```bash
sudo -u postgres psql -f deploy/db-setup.sql   # 비밀번호는 파일에서 수정
```

### 3) 배포 디렉터리와 계정

```bash
sudo useradd -r -s /usr/sbin/nologin portal
sudo mkdir -p /opt/partner-portal
sudo chown portal:portal /opt/partner-portal
```

### 4) 소스 받고 빌드

```bash
cd /opt
sudo git clone <저장소 URL> partner-portal-src
cd partner-portal-src
mvn clean package -DskipTests
sudo install -m 644 target/partner-portal.jar /opt/partner-portal/partner-portal.jar
```

### 5) 환경변수 파일

```bash
sudo cp .env.example /opt/partner-portal/.env
sudo nano /opt/partner-portal/.env        # DB 비밀번호, APP_BASE_URL, 메일 등 설정
sudo chown portal:portal /opt/partner-portal/.env
sudo chmod 600 /opt/partner-portal/.env
```

### 6) systemd 등록

```bash
sudo cp deploy/partner-portal.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now partner-portal
sudo journalctl -u partner-portal -f      # 최초 관리자 비밀번호 확인
```

### 7) (권장) 리버스 프록시 + HTTPS

Nginx 등으로 443 → 127.0.0.1:8080 프록시하고 Let's Encrypt 인증서를 적용하세요.
`APP_BASE_URL` 은 실제 공개 주소(https)로 설정해야 비밀번호 초기화 링크가 올바르게 생성됩니다.

### 이후 업데이트

```bash
cd /opt/partner-portal-src
git pull
./deploy/deploy.sh
```

---

## 설정 항목 (환경변수)

`.env.example` 참고. 주요 항목:

- `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` — PostgreSQL 접속
- `APP_BASE_URL` — 메일 링크 생성용 공개 주소
- `APP_BRAND` / `APP_COMPANY` — 화면에 표시할 포털명·회사명
- `ADMIN_LOGIN_ID` / `ADMIN_PASSWORD` — 최초 관리자(비우면 랜덤 생성 후 로그 출력)
- `SPRING_MAIL_*` — SMTP. 미설정 시 초기화 링크는 로그로만 출력(개발·초기 운영용)
- `JPA_DDL_AUTO` — `update`(기본) / 운영 안정화 후 `validate` 권장

## 디렉터리 구조

```
src/main/java/com/partnerportal
├─ config/        보안·프로퍼티 설정
├─ domain/        JPA 엔티티
├─ repository/    Spring Data JPA
├─ security/      인증 처리(UserDetails, 로그인 성공/실패 핸들러)
├─ service/       업무 로직
└─ web/           컨트롤러 + 폼
src/main/resources/templates   Thymeleaf 화면
deploy/           DB 스크립트, systemd 유닛, 배포 스크립트
```
