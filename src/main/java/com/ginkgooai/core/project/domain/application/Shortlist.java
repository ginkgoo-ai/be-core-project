package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shortlist")
public class Shortlist extends BaseAuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String workspaceId;
    
    private String projectId;
    
    private String ownerId;

    @Enumerated(EnumType.STRING)
    private OwnerType ownerType;
    
    private String name;
    
    private String description;
    
    @OneToMany(mappedBy = "shortlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShortlistItem> items;
    
    @Version
    private Long version;
}