package com.thebizio.biziosupport;

import com.google.gson.JsonObject;
import com.thebizio.biziosupport.config.PathBasedConfigResolver;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.io.FileWriter;
import java.io.IOException;

@SpringBootApplication
public class BizioSupportApplication {

	public static void main(String[] args) {
		SpringApplication.run(BizioSupportApplication.class, args);
	}

	@Bean
	@ConditionalOnMissingBean(PathBasedConfigResolver.class)
	public KeycloakConfigResolver keycloakConfigResolver() {
		return new PathBasedConfigResolver();
	}

}
