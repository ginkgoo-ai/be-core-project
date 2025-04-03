// TalentBasicResponse.java
package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.talent.Talent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Basic talent information response")
public class TalentBasicResponse {
    @Schema(description = "Talent ID")
    private String id;

    @Schema(description = "Talent name")
    private String name;

    @Schema(description = "Talent email")
    private String email;

    public static TalentBasicResponse from(Talent talent) {
        return TalentBasicResponse.builder()
            .id(talent.getId())
            .name(talent.getName())
            .email(talent.getEmail())
            .build();
    }
}