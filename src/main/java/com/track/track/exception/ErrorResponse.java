package com.track.track.exception;

import lombok.Getter;

/**
 * 예외를 HTTP 응답으로 바꿔주는 객체
 */
@Getter
public class ErrorResponse {
    private final String code;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }
}
