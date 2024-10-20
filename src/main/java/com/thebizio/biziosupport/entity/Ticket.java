package com.thebizio.biziosupport.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thebizio.biziosupport.convertor.SetConvertor;
import com.thebizio.biziosupport.enums.*;
import com.thebizio.biziosupport.generator.SecureRandomReferenceIdGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;


@Entity
@Table(name = "tickets")
@Getter
@Setter
@ToString
@NoArgsConstructor
//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Ticket extends BaseEntity{

    @Id
    @GeneratedValue(generator = "uuid4")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @GeneratorType(type = SecureRandomReferenceIdGenerator.class, when = GenerationTime.INSERT)
    @Column(name = "ticket_ref_no", unique = true, nullable = false, updatable = false, length = 64)
    private String ticketRefNo;

    private TicketType ticketType;

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String title;

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
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

    @Column(columnDefinition = "boolean default true")
    private Boolean issueOnWebBrowser;

    @Convert(converter = SetConvertor.class)
    private Set<String> attachments = new HashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private Set<TicketMessage> messages = new HashSet<>();
}
