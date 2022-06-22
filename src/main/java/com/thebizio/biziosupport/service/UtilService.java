package com.thebizio.biziosupport.service;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UtilService {

	public AccessToken getKeycloakPrincipal() {
		return ((KeycloakPrincipal) (SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
				.getKeycloakSecurityContext().getToken();
	}

	public String getAuthUserName() {
		return getKeycloakPrincipal().getPreferredUsername();
	}

	public String getAuthUserEmail() {
		return getKeycloakPrincipal().getEmail();
	}


}
