package com.thebizio.biziosupport.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thebizio.biziosupport.UserDto;
import com.thebizio.biziosupport.util.testcontainers.BaseTestContainer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.thebizio.biziosupport.util.testcontainers.BaseTestContainer.KEYCLOAK_RESOURCE;

@Service
public class UtilTestService {

//    @Autowired
//    private ClientKeycloakMockService keycloakMockService;

    @Autowired
    private ObjectMapper objectMapper;

    Map<String, String> tokenMap = new HashMap<>();

    private String getToken(UserDto userDto) {

        // token if already present return directly
        if(tokenMap.containsKey(userDto.getUsername())) return "Bearer " + tokenMap.get(userDto.getUsername());

        // create user if not exists
        if(!kcHasUser(userDto)) BaseTestContainer.demoUserCreation(userDto);

        Keycloak client = KeycloakBuilder.builder()
                .serverUrl(BaseTestContainer.keycloak.getAuthServerUrl())
                .realm(userDto.getRealm())
                .clientId(KEYCLOAK_RESOURCE)
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .build();

        tokenMap.put(userDto.getUsername(), client.tokenManager().getAccessTokenString());

        return "Bearer " + tokenMap.get(userDto.getUsername());
    }

    private boolean kcHasUser(UserDto userDto) {
        return !BaseTestContainer.keycloakAdminClient.realm(userDto.getRealm()).users().search(userDto.getUsername(), true).isEmpty();
    }

    public MockHttpServletRequestBuilder setUp(MockHttpServletRequestBuilder builder, UserDto userDto) {
        return builder.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .header("Authorization", getToken(userDto));
    }

    public MockHttpServletRequestBuilder setUp(MockHttpServletRequestBuilder builder, Object body, UserDto userDto)
            throws JsonProcessingException {
        return builder.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .header("Authorization", getToken(userDto))
                .content(objectMapper.writeValueAsString(body));
    }

    public MockHttpServletRequestBuilder setUpWithoutToken(MockHttpServletRequestBuilder builder) {
        return builder.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
    }

    public MockHttpServletRequestBuilder setUpWithoutToken(MockHttpServletRequestBuilder builder, Object body)
            throws JsonProcessingException {
        return builder.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }
}
