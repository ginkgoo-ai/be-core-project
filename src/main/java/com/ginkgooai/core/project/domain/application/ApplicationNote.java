package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.BaseLogicalDeleteEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "application_note")
public class ApplicationNote extends BaseLogicalDeleteEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id")
    private Application application;
    
    @Column(columnDefinition = "TEXT")
    private String content;
}