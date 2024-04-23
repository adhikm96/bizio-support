package com.thebizio.biziosupport.service.rmq;

import com.google.gson.Gson;
import com.thebizio.biziosupport.dto.DomainTicketCreateDto;
import com.thebizio.biziosupport.dto.mq.IncomingEventDto;
import com.thebizio.biziosupport.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BizioEventListener {

    Logger logger = LoggerFactory.getLogger(BizioEventListener.class);

    @Autowired
    private TicketService ticketService;

    @RabbitListener(queues = "${hostname}")
    public void consumeBizioEventsFromQueue(Message obj) {
        Gson gson = new Gson();
        IncomingEventDto incomingEventDto = gson.fromJson(new String(obj.getBody()), IncomingEventDto.class);

        switch (incomingEventDto.getKey()) {
            case "BZ-ADMIN|CTR|TICKET":
                try {
                    DomainTicketCreateDto domainTicketCreateDto = gson.fromJson(incomingEventDto.getPayload(), DomainTicketCreateDto.class);
                    if (domainTicketCreateDto.getUsername() != null && domainTicketCreateDto.getTitle() != null && domainTicketCreateDto.getDescription() != null) {
                        ticketService.createEmailTicket(domainTicketCreateDto);
                        logger.info("Email ticket event: " + domainTicketCreateDto);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                break;
            default:
                log.info("event key " + incomingEventDto.getKey() + " skipped");
        }
    }
}
