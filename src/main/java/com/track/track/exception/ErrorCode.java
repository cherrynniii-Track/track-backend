package com.track.track.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 사용하는 예외 정보를 정의하는 열거형
 * HTTP 상태 코드와 에러 메시지를 함께 관리
 */
@Getter
public enum ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트입니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 프로젝트에 접근할 권한이 없습니다."),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    CATEGORY_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 카테고리입니다."),
    CATEGORY_PROJECT_MISMATCH(HttpStatus.BAD_REQUEST, "해당 카테고리는 요청한 프로젝트에 속하지 않습니다."),

    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 작업입니다."),
    INVALID_PAGE_NUMBER(HttpStatus.BAD_REQUEST, "페이지 번호는 0 이상이어야 합니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "페이지 크기는 1 이상 100 이하여야 합니다."),
    INVALID_DUE_DATE_RANGE(HttpStatus.BAD_REQUEST, "마감일 시작 날짜는 종료 날짜보다 늦을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}