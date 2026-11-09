-- PostgreSQL 초기 설정. psql 로 superuser(postgres) 접속 후 실행하세요.
--   sudo -u postgres psql -f deploy/db-setup.sql
-- 비밀번호는 실제 값으로 바꾸세요.

CREATE USER portal WITH PASSWORD 'change-this-password';
CREATE DATABASE partner_portal OWNER portal ENCODING 'UTF8' LC_COLLATE 'C' LC_CTYPE 'C' TEMPLATE template0;
GRANT ALL PRIVILEGES ON DATABASE partner_portal TO portal;
