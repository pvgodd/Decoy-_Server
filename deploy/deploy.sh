#!/usr/bin/env bash
# 서버에서 실행하는 배포 스크립트.
#   git pull 후  ./deploy/deploy.sh  실행
# 최초 1회는 README 의 "서버 설치" 절차(JDK/Postgres 설치, DB 생성, .env 작성,
# systemd 유닛 등록)를 먼저 끝내야 합니다.
set -euo pipefail

APP_DIR=/opt/partner-portal
JAR_NAME=partner-portal.jar
SERVICE=partner-portal

echo "==> 빌드"
mvn -q clean package -DskipTests

echo "==> 산출물 배치"
sudo install -m 644 target/partner-portal.jar "$APP_DIR/$JAR_NAME"

echo "==> 서비스 재시작"
sudo systemctl restart "$SERVICE"
sleep 3
sudo systemctl --no-pager status "$SERVICE" | head -n 12

echo "==> 완료. 로그: journalctl -u $SERVICE -f"
