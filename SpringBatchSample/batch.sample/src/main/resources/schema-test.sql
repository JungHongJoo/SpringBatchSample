DROP TABLE IF EXISTS MEMBER;
DROP TABLE IF EXISTS EVEN_MEMBER_ID;

CREATE TABLE MEMBER_INFO COMMENT '회원' (
    MEMBER_ID          NUMBER(10)       NOT NULL    COMMENT '회원ID',
    MEMBER_NAME        VARCHAR(100)     NOT NULL    COMMENT '회원명',
    MEMBER_FLAG        VARCHAR(1)       NOT NULL    COMMENT '회원 플래그'
    , PRIMARY KEY (MEMBER_ID)
    );


CREATE TABLE MEMBER_READ_HIST COMMENT '멤버 이력 테이블' (
    MEMBER_ID      NUMBER(10)     NOT NULL    COMMENT '회원ID',
    FOREIGN KEY (MEMBER_ID)
    REFERENCES MEMBER_INFO (MEMBER_ID)
    );