package com.thebizio.biziosupport.util.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.concurrent.atomic.AtomicInteger;

public class Listener {
    public AtomicInteger emailNotiCnt = new AtomicInteger(0);
    public AtomicInteger eventManagerNotiCnt = new AtomicInteger(0);

    @RabbitListener(id="email-notification", queues="email-queue")
    public void listenNotificationEmail(@Payload Object body) {
        emailNotiCnt.set(emailNotiCnt.get()+1);
        System.out.println("Listened email notification");
        System.out.println(body.toString());
    }

    @RabbitListener(id="event-manager", queues="bz-queue")
    public void listenBZQueue(@Payload Object body) {
        eventManagerNotiCnt.set(eventManagerNotiCnt.get()+1);
        System.out.println("Listened bz events");
        System.out.println(body.toString());
    }
}
