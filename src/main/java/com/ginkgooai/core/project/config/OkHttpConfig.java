package com.ginkgooai.core.project.config;

import feign.Client;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpConfig {

	@Bean
	public Client feignClient() {
		return new feign.okhttp.OkHttpClient(new OkHttpClient());
	}

}
