package com.ginkgooai.core.project.domain.application;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "application_video_mapping")
public class ApplicationVideoMapping {
    @Id
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
    
    private String videoSubmissionId;
    
    private Integer displayOrder;
    
    private String description;
}