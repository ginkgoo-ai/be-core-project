package com.ginkgooai.core.project.domain.talent;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
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
@Table(name = "imdb_movie_item")
public class ImdbMovieItem extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String title;
    
    private String cover;
    
    private String role;
    
    private String year;
    
    private String rating;
    
    private String mediaType;
    
    private String titleUrl;
}