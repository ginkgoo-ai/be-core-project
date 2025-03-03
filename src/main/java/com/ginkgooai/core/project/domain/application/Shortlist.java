package com.ginkgooai.core.project.domain.application;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shortlist")
public class Shortlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String workspaceId;
    
    private String projectId;
    
    private String ownerId;
    
    private String name;
    
    private String description;
    
    @OneToMany(mappedBy = "shortlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShortlistItem> items;
    
    private String createdBy;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}