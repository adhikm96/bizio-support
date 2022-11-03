package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailsDto {

    private String userName;
    private String firstName;
    private String lastName;
    private String email;
}
