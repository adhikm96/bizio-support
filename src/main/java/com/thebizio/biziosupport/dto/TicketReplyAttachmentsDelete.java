package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TicketReplyAttachmentsDelete {

    @NotNull(message = "must not be null")
    private UUID ticketMessageId;

    private String ticketRefNo;

    @NotNull
    private Set<String> attachments = new HashSet<>();
}
