package com.thebizio.biziosupport.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thebizio.biziosupport.convertor.SetConvertor;
import com.thebizio.biziosupport.enums.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ticket_messages")
@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TicketMessage extends BaseEntity{

    @Id
    @GeneratedValue(generator = "uuid4")
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String message;
    private String owner;

    private MessageType messageType;

    @Convert(converter = SetConvertor.class)
    private Set<String> attachments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", referencedColumnName = "id")
    private Ticket ticket;

}
