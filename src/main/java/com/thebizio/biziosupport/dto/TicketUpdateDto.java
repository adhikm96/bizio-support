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
public class TicketUpdateDto {

    private TicketType ticketType;

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String title;

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String description;

    private Set<String> attachments = new HashSet<>();
}
