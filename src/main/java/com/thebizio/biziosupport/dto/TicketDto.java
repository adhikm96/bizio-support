package com.thebizio.biziosupport.dto;

import com.thebizio.biziosupport.enums.TicketStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TicketDto {

    private UUID id;
    private String title;
    private String attachments;
    private String conversation;
    private TicketStatus status;
}
