package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponseEntity {

    private Integer statusCode;
    private String message;
    private AdminUserDetailsDto resObj;

}
