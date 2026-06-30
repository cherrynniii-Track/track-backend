package com.track.track.domain;

import jakarta.persistence.EntityListeners;         // 엔티티가 저장되거나 수정될 때 자동 실행
import jakarta.persistence.MappedSuperclass;        // 테이블이 아니라 부모 클래스
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;


@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}