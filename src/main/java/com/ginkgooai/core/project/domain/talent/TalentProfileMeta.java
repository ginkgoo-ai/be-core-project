package com.ginkgooai.core.project.domain.talent;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import com.ginkgooai.core.project.dto.TalentProfileData;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "talent_profile_meta")
public class TalentProfileMeta extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String source;  // "IMDB" or "Spotlight"
    
    private String sourceUrl;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private TalentProfileData data;
}