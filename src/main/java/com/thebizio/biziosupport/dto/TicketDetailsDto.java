package com.thebizio.biziosupport.dto;

import com.thebizio.biziosupport.convertor.SetConvertor;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.*;
import com.thebizio.biziosupport.generator.SecureRandomReferenceIdGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TicketDetailsDto {

    private UUID id;
    private String ticketRefNo;
    private TicketType ticketType;
    private String title;
    private String description;
    private TicketStatus status;
    private String openedBy;
    private String assignedTo;
    private String closedBy;
    private DeviceType deviceType;
    private OsEnum os;
    private ApplicationEnum application;
    private BrowserEnum browser;
    private String osVersion;
    private String applicationVersion;
    private String browserVersion;
    private Set<String> attachments = new HashSet<>();

}
