package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shortlist_item")
public class ShortlistItem extends BaseAuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "shortlist_id")
    private Shortlist shortlist;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToMany
    @JoinTable(name = "shortlist_item_submission_mapping", joinColumns = @JoinColumn(name = "shortlist_item_id"), inverseJoinColumns = @JoinColumn(name = "submission_id"))
    private List<Submission> submissions = new ArrayList<>();

    private Integer sortOrder;
}