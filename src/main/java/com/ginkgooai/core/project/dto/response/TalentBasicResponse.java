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

    @Schema(description = "Talent's first name")
    private String firstName;

    @Schema(description = "Talent's last name")
    private String lastName;

    @Schema(description = "Talent email")
    private String email;

    @Schema(description = "URL of the talent's profile photo")
    private String profilePhotoUrl;

    public static TalentBasicResponse from(Talent talent) {
        return TalentBasicResponse.builder()
            .id(talent.getId())
            .firstName(talent.getFirstName())
            .lastName(talent.getLastName())
            .email(talent.getEmail())
            .profilePhotoUrl(talent.getProfilePhotoUrl())
            .build();
    }
}