package com.ginkgooai.core.project.domain.application;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shortlist_item")
public class ShortlistItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortlist_id")
    private Shortlist shortlist;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;
    
    private String notes;
    
    private Integer order;
    
    private String addedBy;
    
    @CreationTimestamp
    private LocalDateTime addedAt;
}