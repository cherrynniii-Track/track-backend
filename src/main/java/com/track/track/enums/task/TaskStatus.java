package com.track.track.enums.task;

public enum TaskStatus {
    TODO,               // 시작 전
    IN_PROGRESS,        // 진행 중
    ON_HOLD,            // 일시 중단, 나중에 재개 가능
    COMPLETED,          // 완료
    CANCELED            // 취소, 더 이상 진행하지 않음
}
