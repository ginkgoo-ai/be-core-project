package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.talent.Contact;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.KnownForItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@Schema(description = "Response object containing talent information")
public class TalentResponse {
    @Schema(description = "Unique identifier for the talent")
    private String id;

    @Schema(description = "Workspace identifier that the talent belongs to")
    private String workspaceId;

    @Schema(description = "User's first name")
    private String firstName;

    @Schema(description = "User's last name")
    private String lastName;

    @Schema(description = "Contact email address")
    private String email;

    @Schema(description = "IMDB profile URL")
    private String imdbProfileUrl;

    @Schema(description = "Spotlight profile URL")
    private String spotlightProfileUrl;

    @Schema(description = "URL of the talent's profile photo")
    private String profilePhotoUrl;

    @Schema(description = "List of notable works the talent is known for")
    private Set<KnownForItem> knownFor;

    @Schema(description = "Personal details including physical attributes, skills, and other relevant information")
    private Map<String, String> personalDetails;

    @Schema(description = "Current status of the talent (e.g., ACTIVE, INACTIVE)")
    private String status;

    @Schema(description = "Timestamp when the talent record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the talent record")
    private LocalDateTime updatedAt;

    @Schema(description = "List of contact persons associated with the talent (agents, managers, etc.)")
    private List<Contact> contacts;

    @Schema(description = "List of applications submitted by the talent")
    private List<ApplicationBriefResponse> applications;

    @Schema(description = "List of video submissions by the talent")
    private List<SubmissionBriefResponse> submissions;


    public static TalentResponse from(Talent talent) {
        return TalentResponse.builder()
            .id(talent.getId())
            .workspaceId(talent.getWorkspaceId())
            .lastName(talent.getName())
            .email(talent.getEmail())
            .imdbProfileUrl(talent.getImdbProfileUrl())
            .spotlightProfileUrl(talent.getSpotlightProfileUrl())
            .profilePhotoUrl(talent.getProfilePhotoUrl())
            .contacts(talent.getContacts())
            .personalDetails(talent.getPersonalDetails())
            .status(talent.getStatus().name())
            .createdAt(talent.getCreatedAt())
            .updatedAt(talent.getUpdatedAt())
            .build();
    }
}