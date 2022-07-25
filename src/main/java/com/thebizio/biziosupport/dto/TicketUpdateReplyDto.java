package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TicketUpdateReplyDto {

    @NotNull(message = "must not be null")
    private UUID ticketMessageId;

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String message;

    private Set<String> attachments = new HashSet<>();
}
