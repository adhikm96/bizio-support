package com.thebizio.biziosupport.service;

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.keycloak.admin.client.Keycloak;

@Service
public class KeycloakBizioAdminClientService extends KeycloakClientService{

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${bizio-admin.keycloak-admin-resource}")
    private String keycloakAdminResource;

    @Value("${bizio-admin.keycloak-admin-username}")
    private String keycloakAdminUsername;

    @Value("${bizio-admin.keycloak-admin-password}")
    private String keycloakAdminPassword;

    @Override
    public Keycloak getKeycloak() {
        super.keycloakRealm = keycloakRealm;
        return Keycloak.getInstance(keycloakAuthUrl, keycloakRealm, keycloakAdminUsername, keycloakAdminPassword,
                keycloakAdminResource);
    }

    public UserRepresentation getUserRepresentation(String userId){
        UserResource userResource = getUsersResource().get(userId);
        UserRepresentation userRepresentation = userResource.toRepresentation();
        return userRepresentation;
    }
}
