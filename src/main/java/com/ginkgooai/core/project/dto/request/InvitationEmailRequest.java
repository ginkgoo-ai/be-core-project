package com.ginkgooai.core.project.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class InvitationEmailRequest {

    private String emailTemplateType;

    private List<String> applicationIds;

}
