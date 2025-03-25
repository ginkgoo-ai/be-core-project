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

import java.util.List;

@FeignClient(name = "identity-service", url="${core-identity-uri}", configuration = FeignConfig.class)
public interface IdentityClient {
    /**
     * Retrieves user information by user ID.
     *
     * <p>This method sends a GET request to the identity service's <code>/users/{id}</code> endpoint to
     * obtain details for the specified user. The response is wrapped in a <code>ResponseEntity</code> to include
     * both the user information and the HTTP status code.
     *
     * @param id the unique identifier of the user
     * @return a ResponseEntity containing the user's information if found
     */
    @GetMapping("/users/{id}")
    ResponseEntity<UserInfo> getUserById(@PathVariable String id);

    /**
     * Retrieves user information for multiple user IDs.
     *
     * Sends a GET request to the identity service to obtain details for each user identified in the provided list.
     *
     * @param userIds a list of user IDs for which user information is requested
     * @return a ResponseEntity containing a list of UserInfo objects corresponding to the provided user IDs
     */
    @GetMapping("/users")
    ResponseEntity<List<UserInfo>> getUsersByIds(List<String> userIds);
    
    /**
     * Validates whether a user with the specified ID exists.
     *
     * @param id the identifier of the user to validate
     * @return true if the user exists; false otherwise
     */
    @GetMapping("/users/validate/{id}")
    boolean validateUser(@PathVariable String id);

    @GetMapping("/guest-codes")
    ResponseEntity<GuestCodeResponse> generateGuestCode(@RequestBody GuestCodeRequest request);

}

