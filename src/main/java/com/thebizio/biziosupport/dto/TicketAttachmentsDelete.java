package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class TicketAttachmentsDelete {

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String ticketRefNo;

    @NotNull
    private Set<String> attachments = new HashSet<>();
}
