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
public class ClientKeycloakMockService {

    @Value("${bizio-client.keycloak-realm}")
    private static final String REALM = "client";

    private static final String DEFAULT_REALM = "master";

    private static final int PORT = 8063;

    private static final String HOST = "localhost";

    private KeycloakMock mock = new KeycloakMock(
            aServerConfig().withDefaultHostname(HOST).withPort(PORT).withDefaultRealm(DEFAULT_REALM).build());

    public void mockStart() {
        mock.start();
    }

    public void mockStop() {
        mock.stop();
    }


    public String getToken(List<String> roles) {
        if (roles == null) {
            roles = new ArrayList<>();
            roles.add("admin");
        }
        return "Bearer " + mock.getAccessToken(aTokenConfig().withEmail("sample@email.com").withRealm(REALM)
                .withRealmRoles(roles).withPreferredUsername("foobar").build());
    }
}
