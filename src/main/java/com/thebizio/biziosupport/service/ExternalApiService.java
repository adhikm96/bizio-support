package com.thebizio.biziosupport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thebizio.biziosupport.dto.ApiResponseEntity;
import com.thebizio.biziosupport.dto.UserDetailsDto;
import com.thebizio.biziosupport.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalApiService {

    @Value("${bizio-admin-center-url}")
    private String bizioAdminCenterUrl;

    @Autowired
    private RestTemplate restTemplate;

    public UserDetailsDto searchUser(String userName, Boolean adminSearch){
        String url = null;
        if (adminSearch){
            url = bizioAdminCenterUrl+"/api/v1/internal/users/search/"+userName;
        }else {
            url = bizioAdminCenterUrl+"/api/v1/internal/users/search/user/"+userName;
        }
        ApiResponseEntity response = null;
        ResponseEntity<String> errorResponse = null;
        UserDetailsDto userNameFound = null;
        try {
            response = restTemplate.getForEntity(url,ApiResponseEntity.class).getBody();
        }catch(HttpStatusCodeException e) {
            errorResponse = ResponseEntity.status(e.getRawStatusCode()).body(e.getResponseBodyAsString());
        }
        if(response == null){
            if(errorResponse != null){
                ApiResponseEntity errorResBody = null;
                try {
                    errorResBody = new ObjectMapper().readValue(errorResponse.getBody(), ApiResponseEntity.class);
                }catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                if(errorResponse.getStatusCode().value() == 400){
                    if(errorResBody.getMessage() != null){
                        throw new NotFoundException(errorResBody.getMessage());
                    }else {
                        throw new NotFoundException("error message not found");
                    }
                }else {
                    throw new NotFoundException(errorResponse.getStatusCode().value()+" error");
                }
            }else {
                throw new NotFoundException("not able to get error");
            }
        }else {
            if (response.getStatusCode().equals(200)){
                userNameFound = response.getResObj();
            }else {
                throw new NotFoundException(response.getStatusCode().toString()+" Error");
            }
        }
        if(userNameFound == null){
            throw new NotFoundException("user name is null");
        }
        return userNameFound;
    }
}
