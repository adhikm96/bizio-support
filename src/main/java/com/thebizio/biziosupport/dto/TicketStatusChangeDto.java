package com.thebizio.biziosupport.dto;

import com.thebizio.biziosupport.enums.TicketStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TicketStatusChangeDto {

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String ticketRefNo;

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String status;
}
