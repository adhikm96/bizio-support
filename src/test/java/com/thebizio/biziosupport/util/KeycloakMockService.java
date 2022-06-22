package com.thebizio.biziosupport.util;

import com.tngtech.keycloakmock.api.KeycloakMock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.keycloakmock.api.ServerConfig.aServerConfig;
import static com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig;

@Configuration
public class KeycloakMockService {

	@Value("${keycloak.realm}")
	private static final String REALM = "login-app";

	private static final String DEFAULT_REALM = "master";

	private static final int PORT = 8062;

	private static final String HOST = "localhost";

	@Value("${bizio-admin-role}")
	private String adminRole;

	private KeycloakMock mock = new KeycloakMock(
			aServerConfig().withDefaultHostname(HOST).withPort(PORT).withDefaultRealm(DEFAULT_REALM).build());

	public void mockStart() {
		mock.start();
	}

	public void mockStop() {
		mock.stop();
	}

	@Bean
	public KeycloakMock getKeycloakMock() {
//		mockStart();
		return mock;
	}

	public String getToken(List<String> roles) {
		if (roles == null) {
			roles = new ArrayList<>();
			roles.add(adminRole);
		}
		return "Bearer " + mock.getAccessToken(aTokenConfig().withEmail("sample@email.com").withRealm(REALM)
				.withRealmRoles(roles).withPreferredUsername("foobar").build());
	}

	public String getAdminToken() {
		return "Bearer " + mock.getAccessToken(aTokenConfig().withEmail("sample@email.com").withRealm(REALM)
				.withRealmRole(adminRole).withPreferredUsername("foobar").build());
	}

	public List<String> getAdminRole() {
		List<String> roles = new ArrayList<>();
		roles.add(adminRole);
		return roles;
	}
}
