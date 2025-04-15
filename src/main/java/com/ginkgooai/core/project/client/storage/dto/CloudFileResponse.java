package com.ginkgooai.core.project.client.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cloud file data transfer object")
public class CloudFileResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Schema(description = "File ID")
    private String id;

    private String originalName;

    private String storageName;

    private String storagePath;

    private String fileType;

    private Long fileSize;

    private String videoThumbnailId;

    private String videoThumbnailUrl;

    private Long videoDuration;

    private String videoResolution;

	private String createdAt;

	private String updatedAt;

}
