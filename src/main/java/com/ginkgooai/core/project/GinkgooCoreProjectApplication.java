package com.ginkgooai.core.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import com.ginkgooai.core.project.config.security.SecurityConfig;

@SpringBootApplication
@EnableAsync
@EnableFeignClients
public class GinkgooCoreProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(GinkgooCoreProjectApplication.class, args);
	}

}
