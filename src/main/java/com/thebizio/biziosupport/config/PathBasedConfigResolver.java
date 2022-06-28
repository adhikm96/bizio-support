package com.thebizio.biziosupport.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PathBasedConfigResolver implements KeycloakConfigResolver {

    private final ConcurrentHashMap<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    private static AdapterConfig adapterConfig;

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        String path = request.getURI();
        int multitenantIndex = path.indexOf("v1/");
        if (multitenantIndex == -1) {
            throw new IllegalStateException("Not able to resolve realm from the request path!");
        }

        String realm = path.substring(path.indexOf("v1/")).split("/")[1];
        if (realm.contains("?")) {
            realm = realm.split("\\?")[0];
        }

        if (!cache.containsKey(realm)) {

            InputStream is = getClass().getResourceAsStream("/" + realm + "-keycloak.json");

//                System.out.println("Calling................");
//                String text = new BufferedReader(
//                        new InputStreamReader(is, StandardCharsets.UTF_8))
//                        .lines()
//                        .collect(Collectors.joining("\n"));
//
//                System.out.println(text);

            cache.put(realm, KeycloakDeploymentBuilder.build(is));
        }

        return cache.get(realm);
    }

    static void setAdapterConfig(AdapterConfig adapterConfig) {
        PathBasedConfigResolver.adapterConfig = adapterConfig;
    }

}
