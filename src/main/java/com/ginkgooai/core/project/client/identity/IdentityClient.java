package com.ginkgooai.core.project.client.identity;

import com.ginkgooai.core.common.config.FeignConfig;
import com.ginkgooai.core.project.client.identity.dto.ShareCodeRequest;
import com.ginkgooai.core.project.client.identity.dto.ShareCodeResponse;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "identity-service", url="${core-identity-uri}", configuration = FeignConfig.class)
public interface IdentityClient {
    @GetMapping("/users/{id}")
    ResponseEntity<UserInfoResponse> getUserById(@PathVariable String id);

    @GetMapping("/users")
    ResponseEntity<List<UserInfoResponse>> getUsersByIds(@RequestParam List<String> ids);
    
    @GetMapping("/users/validate/{id}")
    boolean validateUser(@PathVariable String id);

    @GetMapping("/share-codes")
    ResponseEntity<ShareCodeResponse> generateShareCode(@RequestBody ShareCodeRequest request);

	@DeleteMapping("/share-codes/{shareCode}")
	ResponseEntity<Void> revokeShareCode(@PathVariable String shareCode);
}

