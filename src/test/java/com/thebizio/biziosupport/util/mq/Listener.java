package com.thebizio.biziosupport.util.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.concurrent.atomic.AtomicInteger;

public class Listener {

    public static AtomicInteger emailNotiCnt = new AtomicInteger();
    public static AtomicInteger eventManagerNotiCnt = new AtomicInteger();

    @RabbitListener(id="email-notification", queues="email-queue")
    public void listenNotificationEmail(@Payload Object body) {
        emailNotiCnt.set(emailNotiCnt.get()+1);
    }

    @RabbitListener(id="event-manager", queues="bz-queue")
    public void listenBZQueue(@Payload Object body) {
        eventManagerNotiCnt.set(eventManagerNotiCnt.get() + 1);
    }
}
