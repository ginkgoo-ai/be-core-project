package com.ginkgooai.core.project.client.identity;

import com.ginkgooai.core.common.config.FeignConfig;
import com.ginkgooai.core.project.client.identity.dto.GuestCodeRequest;
import com.ginkgooai.core.project.client.identity.dto.GuestCodeResponse;
import com.ginkgooai.core.project.client.identity.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "identity-service", url="${core-identity-uri}", configuration = FeignConfig.class)
public interface IdentityClient {
    @GetMapping("/users/{id}")
    ResponseEntity<UserInfo> getUserById(@PathVariable String id);

    @GetMapping("/users")
    ResponseEntity<List<UserInfo>> getUsersByIds(@RequestParam List<String> ids);
    
    @GetMapping("/users/validate/{id}")
    boolean validateUser(@PathVariable String id);

    @GetMapping("/guest-codes")
    ResponseEntity<GuestCodeResponse> generateGuestCode(@RequestBody GuestCodeRequest request);

}

