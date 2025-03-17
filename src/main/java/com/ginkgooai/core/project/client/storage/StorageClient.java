package com.ginkgooai.core.project.client.storage;

import com.ginkgooai.core.common.config.FeignConfig;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "storage-service", url="${core-storage-uri}", configuration = FeignConfig.class)
public interface StorageClient {

    @GetMapping("/v1/files")
    ResponseEntity<List<CloudFileResponse>> getFileDetails(@RequestParam List<String> fileIds);
}

