package com.thebizio.biziosupport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignDto {

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String adminUserId;

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String ticketRefNo;
}
