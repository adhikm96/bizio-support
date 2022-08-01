package com.thebizio.biziosupport.config;

import com.thebizio.biziosupport.exception.NotFoundException;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PathBasedConfigResolver implements KeycloakConfigResolver {

    @Value("${bizio-admin.keycloak-realm}")
    private String adminKeycloakRealm;
    @Value("${bizio-admin.keycloak-auth-server-url}")
    private String adminKeycloakAuthUrl;
    @Value("${bizio-admin.keycloak-resource}")
    private String adminKeycloakResource;
    @Value("${bizio-admin.keycloak-bearer-only}")
    private Boolean adminBearerOnly;
    @Value("${bizio-center.keycloak-realm}")
    private String clientKeycloakRealm;
    @Value("${bizio-center.keycloak-auth-server-url}")
    private String clientKeycloakAuthUrl;
    @Value("${bizio-center.keycloak-resource}")
    private String clientKeycloakResource;
    @Value("${bizio-center.keycloak-bearer-only}")
    private Boolean clientBearerOnly;

    private final ConcurrentHashMap<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    private static AdapterConfig adapterConfig;

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        String path = request.getURI();
        KeycloakDeployment kcDeployment;
        kcDeployment = cache.get("client");

        int multitenantIndex = path.indexOf("v1/");
        if (multitenantIndex == -1) {
            if (kcDeployment == null){
                AdapterConfig ac = new AdapterConfig();
                ac.setRealm(clientKeycloakRealm);
                ac.setAuthServerUrl(clientKeycloakAuthUrl);
                ac.setResource(clientKeycloakResource);
                ac.setBearerOnly(clientBearerOnly);
                cache.put("client", KeycloakDeploymentBuilder.build(ac));
                kcDeployment = cache.get("client");
            }
            return kcDeployment;
        }

        String realm = path.substring(path.indexOf("v1/")).split("/")[1];
        if (realm.contains("?")) {
            realm = realm.split("\\?")[0];
        }

        kcDeployment = cache.get(realm);

        if (kcDeployment == null) {
            if(realm.equals("admin")){
                AdapterConfig ac = new AdapterConfig();
                ac.setRealm(adminKeycloakRealm);
                ac.setAuthServerUrl(adminKeycloakAuthUrl);
                ac.setResource(adminKeycloakResource);
                ac.setBearerOnly(adminBearerOnly);
                cache.put(realm, KeycloakDeploymentBuilder.build(ac));
            } else if (realm.equals("client")) {
                AdapterConfig ac = new AdapterConfig();
                ac.setRealm(clientKeycloakRealm);
                ac.setAuthServerUrl(clientKeycloakAuthUrl);
                ac.setResource(clientKeycloakResource);
                ac.setBearerOnly(clientBearerOnly);
                cache.put(realm, KeycloakDeploymentBuilder.build(ac));
            }else {
                throw new NotFoundException("realm not found");
            }

            kcDeployment = cache.get(realm);
        }

        return kcDeployment;
    }

    static void setAdapterConfig(AdapterConfig adapterConfig) {
        PathBasedConfigResolver.adapterConfig = adapterConfig;
    }

}
