package com.ginkgooai.core.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SoftDelete(strategy = SoftDeleteType.DELETED)
public abstract class BaseLogicalDeleteEntity extends BaseAuditableEntity {

    @LastModifiedDate
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @LastModifiedBy
    @Column(name = "deleted_by")
    private String deletedBy;
}