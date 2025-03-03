package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.application.SubmissionProcessingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response object containing video submission details")
public class VideoSubmissionResponse {

    @Schema(description = "Unique identifier of the video submission", 
            example = "vid_12345")
    private String id;

    @Schema(description = "URL of the video file", 
            example = "https://storage.example.com/videos/submission_12345.mp4")
    private String videoUrl;

    @Schema(description = "URL of the video thumbnail", 
            example = "https://storage.example.com/thumbnails/submission_12345.jpg")
    private String thumbnailUrl;

    @Schema(description = "Title or name of the video", 
            example = "Monologue - Scene 1")
    private String title;

    @Schema(description = "Description of the video content", 
            example = "Dramatic monologue from Shakespeare's Hamlet")
    private String description;

    @Schema(description = "Duration of the video in seconds", 
            example = "180")
    private Long duration;

    @Schema(description = "Video resolution (e.g., 1920x1080)", 
            example = "1920x1080")
    private String resolution;

    @Schema(description = "Video file size in bytes", 
            example = "15728640")
    private Long fileSize;

    @Schema(description = "MIME type of the video", 
            example = "video/mp4")
    private String mimeType;

    @Schema(description = "Processing status of the video")
    private SubmissionProcessingStatus processingStatus;

    @Schema(description = "Error message if processing failed", 
            example = "File format not supported")
    private String processingError;

    @Schema(description = "Original filename", 
            example = "audition_take1.mp4")
    private String originalFilename;

    @Schema(description = "User who uploaded the video", 
            example = "user_12345")
    private String uploadedBy;

    @Schema(description = "Upload timestamp")
    private LocalDateTime uploadedAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}