DROP TABLE IF EXISTS MEMBER;
DROP TABLE IF EXISTS EVEN_MEMBER_ID;

CREATE TABLE MEMBER_INFO COMMENT '회원' (
    MEMBER_ID          NUMBER(10)       NOT NULL    COMMENT '회원ID',
    MEMBER_NAME        VARCHAR(100)     NOT NULL    COMMENT '회원명',
    MEMBER_FLAG_CURSOR        VARCHAR(1)       NOT NULL    COMMENT '회원 플래그 커서',
    MEMBER_FLAG_PAGING        VARCHAR(1)       NOT NULL    COMMENT '회원 플래그 페이징',
    MEMBER_FLAG_TASKLET        VARCHAR(1)       NOT NULL    COMMENT '회원 플래그 태스크릿',
    PRIMARY KEY (MEMBER_ID)
    );


CREATE TABLE MEMBER_READ_HIST_CURSOR COMMENT '멤버 이력 테이블 커서' (
    MEMBER_ID      NUMBER(10)     NOT NULL    COMMENT '회원ID',
    MEMBER_NAME        VARCHAR(100)    COMMENT '회원명',
    FOREIGN KEY (MEMBER_ID)
    REFERENCES MEMBER_INFO (MEMBER_ID)
    );

CREATE TABLE MEMBER_READ_HIST_PAGING COMMENT '멤버 이력 테이블 페이징' (
    MEMBER_ID      NUMBER(10)     NOT NULL    COMMENT '회원ID',
    MEMBER_NAME        VARCHAR(100)    COMMENT '회원명',
    FOREIGN KEY (MEMBER_ID)
    REFERENCES MEMBER_INFO (MEMBER_ID)
    );

CREATE TABLE MEMBER_READ_HIST_TASKLET COMMENT '멤버 이력 테이블 태스크릿' (
    MEMBER_ID      NUMBER(10)     NOT NULL    COMMENT '회원ID',
    MEMBER_NAME        VARCHAR(100)    COMMENT '회원명',
    FOREIGN KEY (MEMBER_ID)
    REFERENCES MEMBER_INFO (MEMBER_ID)
    );