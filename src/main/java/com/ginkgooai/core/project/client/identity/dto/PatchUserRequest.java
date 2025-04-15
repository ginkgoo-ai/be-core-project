package com.ginkgooai.core.project.client.identity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatchUserRequest {

	private String pictureUrl;

	private String firstName;

	private String lastName;

}
