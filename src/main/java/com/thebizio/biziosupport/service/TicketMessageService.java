package com.thebizio.biziosupport.service;

import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.MessageType;
import com.thebizio.biziosupport.repo.TicketMessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketMessageService {

    @Autowired
    private TicketMessageRepo ticketMessageRepo;

    public void createAssignedToTicketMessageEvent(Ticket ticket){
        List<TicketMessage> ticketMessageFound = ticketMessageRepo.findAllByTicketAndMessageTypeAndMessageLike(ticket, MessageType.EVENT,"%is assigned to%");
        TicketMessage ticketMessage = new TicketMessage();
        if (ticketMessageFound.size() > 0){
            ticketMessage.setMessage(ticket.getTicketRefNo()+" is reassigned to "+ticket.getAssignedTo());
        }else {
            ticketMessage.setMessage(ticket.getTicketRefNo()+" is assigned to "+ticket.getAssignedTo());
        }
        ticketMessage.setTicket(ticket);
        ticketMessage.setMessageType(MessageType.EVENT);
        ticketMessageRepo.save(ticketMessage);
    }
}
