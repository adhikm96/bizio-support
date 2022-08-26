package com.thebizio.biziosupport.repo;

import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketMessageRepo extends JpaRepository<TicketMessage, UUID> {
    Optional<TicketMessage> findFirst1ByTicketTicketRefNoAndMessageTypeOrderByCreatedDateDesc(String ticketRefNo, MessageType messageType);

    List<TicketMessage> findAllByTicketTicketRefNoOrderByCreatedDateDesc(String ticketRefNo);

    List<TicketMessage> findAllByTicketAndMessageTypeAndMessageLike(Ticket ticket, MessageType messageType,String message);

    List<TicketMessage> findAllByTicketAndMessageType(Ticket ticket,MessageType messageType);

    @Query("SELECT COUNT(*) FROM TicketMessage tm WHERE tm.ticket = :ticket AND tm.messageType = :messageType")
    Integer countTicketMessageByTicketAndMessageType(Ticket ticket,MessageType messageType);
}
