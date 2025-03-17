package com.ginkgooai.core.project.client.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Cloud file data transfer object")
public class CloudFileResponse {

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

}
