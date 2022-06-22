package com.thebizio.biziosupport.entity;
import com.thebizio.biziosupport.convertor.SetConvertor;
import com.thebizio.biziosupport.enums.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "tickets")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Ticket{

    @Id
    @GeneratedValue(generator = "uuid4")
    @Column(columnDefinition = "uuid")
    private UUID id;

    private TicketType ticketType;
    private String title;
    private String description;
    private TicketStatus status;
    private String openedBy;
    private String closedBy;
    private DeviceType deviceType;
    private OsEnum os;
    private ApplicationEnum application;
    private BrowserEnum browser;

    @Convert(converter = SetConvertor.class)
    private Set<String> attachments = new HashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketMessage> messages = new HashSet<>();
}
