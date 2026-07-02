package com.track.track.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 발생하는 예외를 표현하는 클래스
 * ErrorCode를 함께 저장하여 상태 코드와 메시지 전달
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    /**
     * ErrorCode를 기반으로 비즈니스 예외를 생성
     * @param errorCode 발생한 예외 정보
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
