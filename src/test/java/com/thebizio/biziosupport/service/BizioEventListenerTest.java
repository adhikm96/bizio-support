package com.thebizio.biziosupport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.thebizio.biziosupport.dto.DomainTicketCreateDto;
import com.thebizio.biziosupport.dto.mq.IncomingEventDto;
import com.thebizio.biziosupport.repo.TicketMessageRepo;
import com.thebizio.biziosupport.repo.TicketRepo;
import com.thebizio.biziosupport.util.BaseTestCase;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Order(Integer.MIN_VALUE)
public class BizioEventListenerTest extends BaseTestCase {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private TicketRepo ticketRepo;

    @Autowired
    private TicketMessageRepo ticketMessageRepo;

    @BeforeAll
    public void beforeAll() {
        ticketMessageRepo.deleteAll();
        ticketRepo.deleteAll();

    }

    @AfterAll
    public void afterAll() {
        ticketMessageRepo.deleteAll();
        ticketRepo.deleteAll();
    }

    @Test
    @Transactional
    @Rollback(value = false)
    public void test_add_message_to_bz_gossip() throws InterruptedException, IOException {

        Gson gson = new Gson();

        DomainTicketCreateDto dto = new DomainTicketCreateDto();
        dto.setUsername("testing");
        dto.setTitle("Setup Domain");
        dto.setDescription("testing.com");


        rabbitTemplate.convertAndSend("bz.gossip", "BZ-ADMIN|CTR|TICKET", new IncomingEventDto("BZ-ADMIN|CTR|TICKET", gson.toJson(dto, DomainTicketCreateDto.class)));

        assertEquals(0, ticketRepo.count());
        assertEquals(0, ticketMessageRepo.count());

        Thread.sleep(1000);

        assertEquals(1, ticketRepo.count());
        assertEquals(1, ticketMessageRepo.count());
        assertEquals(dto.getTitle() , ticketRepo.findAll().get(0).getTitle());
        assertEquals(dto.getDescription() , ticketRepo.findAll().get(0).getDescription());
        assertEquals(dto.getUsername() , ticketRepo.findAll().get(0).getOpenedBy());
    }
}