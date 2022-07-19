package com.thebizio.biziosupport.dto;

import com.thebizio.biziosupport.enums.TicketStatus;
import com.thebizio.biziosupport.enums.TicketType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private TicketType type;
    private String ticketRefNo;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String modifiedBy;

    private String openedBy;
    private String assignedTo;
}
