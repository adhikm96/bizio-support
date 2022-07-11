package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TicketMessageDto {

    private UUID id;
    private String message;
    private String owner;
    private Set<String> attachments = new HashSet<>();
    private UUID ticketId;
    private LocalDateTime createdDate;

}
