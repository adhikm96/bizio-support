package com.thebizio.biziosupport.service;

import com.thebizio.biziosupport.service.TicketService;
import com.thebizio.biziosupport.util.mq.Listener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class EmailSendTest {

    @Autowired
    TicketService ticketService;

    @Autowired
    Listener listener;

    @Test
    void send_email_event_test() {

        listener.emailNotiCnt.set(0);
        listener.eventManagerNotiCnt.set(0);

        ticketService.sendSuccessMail(
                "smaple@email.com",
                "Ticket Assigned",
                "ticket-assigned-notification-to-admin.ftl",
                "fname",
                "lname",
                "uname",
                "admin-uname",
                "TKT-101"
        );

        try {
            // wait email to be queued
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // check email queue on two queues

        assertEquals(1, listener.emailNotiCnt.intValue());
        assertEquals(1, listener.eventManagerNotiCnt.intValue());
    }
}
