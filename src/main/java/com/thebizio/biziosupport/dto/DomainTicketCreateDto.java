package com.thebizio.biziosupport.dto;

import com.thebizio.biziosupport.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
public class DomainTicketCreateDto {

    private String username;
    private String title;
    private String description;
}
