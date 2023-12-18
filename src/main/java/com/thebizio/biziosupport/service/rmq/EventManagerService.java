package com.thebizio.biziosupport.service.rmq;

import com.thebizio.biziosupport.dto.mq.EventDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventManagerService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    public void addEventInQueue(EventDto eventDto){
        rabbitTemplate.convertAndSend("bz.events", "bz.support", eventDto);
    }
}
