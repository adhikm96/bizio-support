package com.thebizio.biziosupport.service;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;


public abstract class KeycloakClientService {

    private static final String DEFAULT_PASSWORD = "bizio";
    protected String keycloakRealm;
    public abstract Keycloak getKeycloak();

    public RealmResource getRealm() {
        return getKeycloak().realm(keycloakRealm);
    }

    public List<RoleRepresentation> getRolesList() {
        return getRolesResource().list();
    }

    public RolesResource getRolesResource() {
        return getRealm().roles();
    }

    public UsersResource getUsersResource() {
        return getRealm().users();
    }

}
