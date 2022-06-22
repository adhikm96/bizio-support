package com.thebizio.biziosupport.controller;

import com.thebizio.biziosupport.dto.TicketCreateDto;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.*;
import com.thebizio.biziosupport.repo.TicketMessageRepo;
import com.thebizio.biziosupport.repo.TicketRepo;
import com.thebizio.biziosupport.service.UtilService;
import com.thebizio.biziosupport.util.KeycloakMockService;
import com.thebizio.biziosupport.util.UtilTestService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TicketControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UtilTestService utilTestService;

    @Autowired
    private KeycloakMockService keycloakMockService;

    @Autowired
    private TicketRepo ticketRepo;

    private Ticket ticket1;

    @Autowired
    private TicketMessageRepo ticketMessageRepo;

    private String getToken() {
        return keycloakMockService.getToken(null);
    }

    @MockBean
    private UtilService utilService;

    @BeforeAll
    public void beforeAll() {
        keycloakMockService.mockStart();
    }

    @AfterAll
    public void afterAll() {
        keycloakMockService.mockStop();
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        Set<String> attachments = new HashSet<>();
        attachments.add("A");
        attachments.add("B");
        attachments.add("C");

        ticket1 = new Ticket();
        ticket1.setTitle("Ticket1");
        ticket1.setDescription("Ticket1 description");
        ticket1.setStatus(TicketStatus.OPEN);
        ticket1.setAttachments(attachments);
        ticketRepo.save(ticket1);

        TicketMessage tm1 = new TicketMessage();
        tm1.setAttachments(attachments);
        tm1.setMessage("tm1 message");
        tm1.setTicket(ticket1);
        ticketMessageRepo.save(tm1);

        TicketMessage tm2 = new TicketMessage();
        tm2.setAttachments(attachments);
        tm2.setMessage("tm2 message");
        tm2.setTicket(ticket1);
        ticketMessageRepo.save(tm2);

        when(utilService.getAuthUserEmail()).thenReturn("Testing@gmail.com");
    }

    @Test
    @DisplayName("test for /tickets create")
    public void create_ticket_test() throws Exception {
        TicketCreateDto dto = new TicketCreateDto();
        Set<String> attachments = new HashSet<>();
        attachments.add("A");
        attachments.add("B");
        attachments.add("C");

        dto.setApplication(ApplicationEnum.BIZIO_MEET);
        dto.setBrowser(BrowserEnum.GOOGLE_CHROME);
        dto.setTicketType(TicketType.BILLING);
        dto.setOs(OsEnum.PC);
        dto.setTitle("New Ticket");
        dto.setDescription("This is ticket description");
        dto.setDeviceType(DeviceType.MOBILE);
        dto.setAttachments(attachments);

        mvc.perform(utilTestService.setUpWithoutToken(get("/api/v1/tickets"))).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/tickets"),dto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

    }

    @Test
    @DisplayName("test for /tickets List")
    public void get_tickets_test() throws Exception {
        mvc.perform(utilTestService.setUpWithoutToken(get("/api/v1/tickets"))).andExpect(status().isUnauthorized());

        //default page number 0 and page size 10
        mvc.perform(utilTestService.setUp(get("/api/v1/tickets"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets[0].id", is(ticket1.getId().toString())))
                .andExpect(jsonPath("$.tickets[0].title", is(ticket1.getTitle())))
                .andExpect(jsonPath("$.tickets[0].attachments", is("3")))
                .andExpect(jsonPath("$.tickets[0].conversation", is("2")))
                .andExpect(jsonPath("$.tickets[0].status", is(TicketStatus.OPEN.toString())))
                .andExpect(jsonPath("$.pageSize", is(10)));

        //pass page number 1 and page size 5
        List list = new ArrayList<>();
        mvc.perform(utilTestService.setUp(get("/api/v1/tickets?page=1&size=5"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", is(list)))
                .andExpect(jsonPath("$.pageSize", is(5)));
    }
}