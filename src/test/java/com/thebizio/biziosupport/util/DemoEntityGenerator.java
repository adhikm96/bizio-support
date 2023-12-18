package com.thebizio.biziosupport.util;

import com.thebizio.biziosupport.UserDto;
import com.thebizio.biziosupport.util.testcontainers.BaseTestContainer;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class DemoEntityGenerator {

    Random random = new Random();

    public String getRandomString(int l) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< l; i++) {
            sb.append('a' + random.nextInt(26));
        }
        return sb.toString();
    }

    public UserDto getAdminUserDto() {
        return new UserDto(
            getRandomString(5) + "@" + getRandomString(4) + ".com",
                getRandomString(8),
                UUID.randomUUID().toString(),
                BaseTestContainer.KEYCLOAK_ADMIN_REALM
        );
    }

    public UserDto getAdminUserDto(String username) {
        return new UserDto(
                getRandomString(5) + "@" + getRandomString(4) + ".com",
                username,
                UUID.randomUUID().toString(),
                BaseTestContainer.KEYCLOAK_ADMIN_REALM
        );
    }

    public UserDto getClientUserDto() {
        return new UserDto(
                getRandomString(5) + "@" + getRandomString(4) + ".com",
                getRandomString(8),
                UUID.randomUUID().toString(),
                BaseTestContainer.KEYCLOAK_CLIENT_REALM
        );
    }

    public UserDto getClientUserDto(String username) {
        return new UserDto(
                getRandomString(5) + "@" + getRandomString(4) + ".com",
                username,
                UUID.randomUUID().toString(),
                BaseTestContainer.KEYCLOAK_CLIENT_REALM
        );
    }

    public UserDto getClientUserDto(String username, String email) {
        return new UserDto(
                email,
                username,
                UUID.randomUUID().toString(),
                BaseTestContainer.KEYCLOAK_CLIENT_REALM
        );
    }
}
