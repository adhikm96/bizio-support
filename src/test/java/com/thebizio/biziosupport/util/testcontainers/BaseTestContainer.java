package com.thebizio.biziosupport.util.testcontainers;

import com.thebizio.biziosupport.UserDto;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import javax.ws.rs.core.Response;
import java.util.*;

public class BaseTestContainer {
    public static RabbitMQContainer rabbit = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"))
            .withExposedPorts(5672);

    public static KeycloakContainer keycloak = OSValidator.isMac() ?
            new KeycloakContainer("sleighzy/keycloak").withEnv("DB_VENDOR", "h2") :
            new KeycloakContainer("quay.io/keycloak/keycloak:16.1.1").withEnv("DB_VENDOR", "h2");

    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withUsername("postgres")
            .withPassword("postgres")
            .withDatabaseName("bz-support-test" + new Random().nextInt(100000));

    public static Keycloak keycloakAdminClient = null;
    public static String KEYCLOAK_ADMIN_REALM = "admin-realm";
    public static String KEYCLOAK_CLIENT_REALM = "client-realm";
    public static String KEYCLOAK_RESOURCE = "test-client";

    static {
        rabbit.start();
        keycloak.start();
        postgres.start();

        keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build();

        RealmRepresentation adminRealmRepresentation = new RealmRepresentation();
        adminRealmRepresentation.setRealm(KEYCLOAK_ADMIN_REALM);
        adminRealmRepresentation.setEnabled(true);
        adminRealmRepresentation.setId(KEYCLOAK_ADMIN_REALM);
        keycloakAdminClient.realms().create(adminRealmRepresentation);

        RealmRepresentation clientRealmRepresentation = new RealmRepresentation();
        clientRealmRepresentation.setRealm(KEYCLOAK_CLIENT_REALM);
        clientRealmRepresentation.setEnabled(true);
        clientRealmRepresentation.setId(KEYCLOAK_CLIENT_REALM);
        keycloakAdminClient.realms().create(clientRealmRepresentation);

        testClientCreation(KEYCLOAK_ADMIN_REALM);
        testClientCreation(KEYCLOAK_CLIENT_REALM);
    }

    public static String demoUserCreation(UserDto userDto){
        return demoUserCreation(userDto.getUsername(), userDto.getEmail(), userDto.getPassword(), userDto.getRealm());
    }

    public static String demoUserCreation(String uname, String email,String pass, String realm) {
        UserRepresentation userRepresentation = new UserRepresentation();

        userRepresentation.setUsername(uname);
        userRepresentation.setEmail(email);
        userRepresentation.setEnabled(true);

        // setting password
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(pass);
        credentialRepresentation.setType("password");
        credentialRepresentation.setTemporary(false);

        if(userRepresentation.getRealmRoles() == null) {
            userRepresentation.setRealmRoles(new ArrayList<>());
        }

        List<CredentialRepresentation> credentialRepresentations = new ArrayList<>();
        credentialRepresentations.add(credentialRepresentation);

        userRepresentation.setCredentials(Arrays.asList(credentialRepresentation));

        // User Created in Keycloak
        Response response = keycloakAdminClient.realm(realm).users().create(userRepresentation);

        if(response.getStatus() >= 400) System.out.println(response.readEntity(ErrorRepresentation.class).getErrorMessage());

        return keycloakAdminClient.realm(realm).users().search(userRepresentation.getUsername(), true).get(0).getId();
    }

    private static void testClientCreation(String realm) {
        // creating a client in master for later user
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(KEYCLOAK_RESOURCE);
        clientRepresentation.setPublicClient(true);
        clientRepresentation.setEnabled(true);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setProtocol("openid-connect");
        if(clientRepresentation.getAttributes() == null) {
            clientRepresentation.setAttributes(new HashMap<>());
        }
        clientRepresentation.getAttributes().put("access.token.lifespan", String.valueOf(3600 * 12));

        Response res = keycloakAdminClient.realm(realm).clients().create(clientRepresentation);
        if(res.getStatus() >= 400) System.out.println(res.readEntity(ErrorRepresentation.class).getErrorMessage());
    }

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.rabbitmq.host",() -> rabbit.getHost());
        registry.add("spring.rabbitmq.port",() -> rabbit.getAmqpPort());
        registry.add("spring.rabbitmq.username",() -> rabbit.getAdminUsername());
        registry.add("spring.rabbitmq.password",() -> rabbit.getAdminPassword());

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("bizio-admin.keycloak-realm", () -> KEYCLOAK_ADMIN_REALM);
        registry.add("bizio-admin.keycloak-auth-server-url", () -> keycloak.getAuthServerUrl());
        registry.add("bizio-admin.keycloak-resource", () -> KEYCLOAK_RESOURCE);

        registry.add("bizio-center.keycloak-realm", () -> KEYCLOAK_CLIENT_REALM);
        registry.add("bizio-center.keycloak-auth-server-url", () -> keycloak.getAuthServerUrl());
        registry.add("bizio-center.keycloak-resource", () -> KEYCLOAK_RESOURCE);
    }
}
