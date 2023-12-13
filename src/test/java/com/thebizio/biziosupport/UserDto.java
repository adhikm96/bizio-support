package com.thebizio.biziosupport;

import com.thebizio.biziosupport.util.testcontainers.BaseTestContainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String email;
    private String username;
    private String password = "Password@123.";
    private String realm = BaseTestContainer.KEYCLOAK_CLIENT_REALM;
}
