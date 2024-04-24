package com.thebizio.biziosupport.controller;

import com.thebizio.biziosupport.UserDto;
import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.*;
import com.thebizio.biziosupport.exception.NotFoundException;
import com.thebizio.biziosupport.repo.TicketMessageRepo;
import com.thebizio.biziosupport.repo.TicketRepo;
import com.thebizio.biziosupport.service.EmailService;
import com.thebizio.biziosupport.service.ExternalApiService;
import com.thebizio.biziosupport.service.UtilService;
import com.thebizio.biziosupport.util.BaseTestCase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ClientTicketControllerTest extends BaseTestCase {
    @Autowired
    private TicketRepo ticketRepo;

    @MockBean
    private ExternalApiService externalApiService;

    @MockBean
    private EmailService emailService;

    private Ticket ticket1;
    private Ticket ticket2;
    private TicketMessage tm1;

    private UserDetailsDto userDetailsDto;

    @Autowired
    private TicketMessageRepo ticketMessageRepo;


    @MockBean
    private UtilService utilService;
    private UserDto clientDto;

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

    @BeforeEach
    public void beforeEach() throws Exception {
        ticketMessageRepo.deleteAll();
        ticketRepo.deleteAll();

        Set<String> attachments = new HashSet<>();
        attachments.add("A");
        attachments.add("B");
        attachments.add("C");

        ticket1 = new Ticket();
        ticket1.setTitle("Ticket1");
        ticket1.setDescription("Ticket1 description");
        ticket1.setStatus(TicketStatus.OPEN);
        ticket1.setAttachments(attachments);
        ticket1.setOpenedBy("TestingUser");
        ticket1.setCreatedBy("TestingUser");
        ticket1.setAssignedTo("TestingUser4");
        ticketRepo.save(ticket1);

        ticket2 = new Ticket();
        ticket2.setTitle("Ticket2");
        ticket2.setDescription("Ticket2 description");
        ticket2.setStatus(TicketStatus.OPEN);
        ticket2.setAttachments(attachments);
        ticket2.setOpenedBy("TestingUser2");
        ticket2.setCreatedBy("TestingUser");
        ticket2.setAssignedTo("TestingUser3");
        ticketRepo.save(ticket2);

        System.out.println("-----------------");
        System.out.println("MESSAGE 1 CREATED");
        tm1 = new TicketMessage();
        tm1.setAttachments(attachments);
        tm1.setMessage("tm1 message");
        tm1.setTicket(ticket1);
        tm1.setOwner("TestingUser");
        tm1.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm1);

        userDetailsDto = new UserDetailsDto();
        userDetailsDto.setUserName("user2");
        userDetailsDto.setFirstName("tom");
        userDetailsDto.setLastName("cruise");
        userDetailsDto.setEmail("tom@gmail.com");

        when(externalApiService.searchUser(any(String.class),any(boolean.class))).thenReturn(userDetailsDto);

        when(utilService.getAuthUserEmail()).thenReturn("Testing@gmail.com");
        when(utilService.getAuthUserName()).thenReturn("TestingUser");

        clientDto = demoEntityGenerator.getClientUserDto("TestingUser", "Testing@gmail.com");
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

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/client/tickets"),dto)).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets"),dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        dto.setTitle(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("must not be null or blank")));

        dto.setTitle("");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("must not be null or blank")));
    }

    @Test
    @DisplayName("test for /tickets List")
    public void get_tickets_test() throws Exception {
        mvc.perform(utilTestService.setUpWithoutToken(get("/api/v1/client/tickets"))).andExpect(status().isUnauthorized());

        //default page number 0 and page size 10
        //openedBy loggedIn user
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets"), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(1)))
                .andExpect(jsonPath("$.tickets[0].id", is(ticket1.getId().toString())))
                .andExpect(jsonPath("$.tickets[0].title", is(ticket1.getTitle())))
                .andExpect(jsonPath("$.tickets[0].attachments", is(3)))
                .andExpect(jsonPath("$.tickets[0].conversation", is(1)))
                .andExpect(jsonPath("$.tickets[0].status", is(TicketStatus.OPEN.toString())))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andDo(print());

        //pass page number 1 and page size 5
        List list = new ArrayList<>();
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets?page=1&size=5"), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", is(list)))
                .andExpect(jsonPath("$.pageSize", is(5)));

        //status open filter
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets?status=Open"), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(1)));

        //status closed filter
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets?status=Closed"), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(0)));

        //ticketRefNo filter
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets?ticketRefNo="+ticket1.getTicketRefNo()), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(1)));

        //assignedTo filter
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets?assignedTo=TestingUser4"), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(1)));

        //status open and assignedTo filter
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets?status=Open&assignedTo=TestingUser4"), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(1)));

    }

    @Test
    @DisplayName("test for /tickets/change-status")
    public void change_ticket_status_test() throws Exception {
        TicketStatusChangeDto dto = new TicketStatusChangeDto();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setStatus("Close");

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/client/tickets/change-status"),dto)).andExpect(status().isUnauthorized());

        //ticket status is open
        assertEquals(TicketStatus.OPEN,ticketRepo.findById(ticket1.getId()).get().getStatus());

        //close ticket
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"),dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        assertEquals(TicketStatus.CLOSED,ticketRepo.findById(ticket1.getId()).get().getStatus());

        //open ticket
        dto.setStatus("Open");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"),dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        assertEquals(TicketStatus.OPEN,ticketRepo.findById(ticket1.getId()).get().getStatus());

        dto.setTicketRefNo(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ticketRefNo", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setStatus(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setStatus("");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("must not be null or blank")));


        TicketStatusChangeDto dto2 = new TicketStatusChangeDto();
        dto2.setTicketRefNo(ticket2.getTicketRefNo());
        dto2.setStatus("Close");

        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"),dto2, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400))).andExpect(jsonPath("$.message", is("user can not change the ticket status")));
    }

    @Test
    @DisplayName("test for /tickets/reply")
    public void reply_ticket_test() throws Exception {
        Set<String> attachments = new HashSet<>();
        attachments.add("D");

        TicketReplyDto dto = new TicketReplyDto();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setMessage("This is coming from reply to ticket api");
        dto.setAttachments(attachments);

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/client/tickets/reply"),dto)).andExpect(status().isUnauthorized());

        //Two messages inside ticket
        assertEquals(1,ticketRepo.findById(ticket1.getId()).get().getMessages().size());
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/reply"),dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //Three messages inside ticket
        assertEquals(2,ticketRepo.findById(ticket1.getId()).get().getMessages().size());

        dto.setTicketRefNo("");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/reply"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ticketRefNo", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setMessage("");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/reply"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("must not be null or blank")));

        ticket1.setAssignedTo(null);
        ticketRepo.save(ticket1);
        dto.setMessage("This is coming from reply to ticket api");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/reply"),dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
    }

    @Test
    @DisplayName("test for /tickets/thread/{ticketRefNo}")
    public void get_thread_ticket_test() throws Exception {
        mvc.perform(utilTestService.setUpWithoutToken(get("/api/v1/client/tickets/thread/"+ticket1.getTicketRefNo()))).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets/thread/"+ticket1.getTicketRefNo()), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")))
                .andExpect(jsonPath("$.resObj", hasSize(1)))
                .andExpect(jsonPath("$.resObj[0].id", is(tm1.getId().toString())))
                .andExpect(jsonPath("$.resObj[0].message", is(tm1.getMessage())))
                .andExpect(jsonPath("$.resObj[0].ticketId", is(ticket1.getId().toString())))
                .andExpect(jsonPath("$.resObj[0].ticketRefNo", is(ticket1.getTicketRefNo())));
    }

    @Test
    @DisplayName("test for /tickets/{ticketRefNo}")
    public void get_ticket_test() throws Exception {
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets/"+ticket1.getTicketRefNo()), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")))
                .andExpect(jsonPath("$.resObj.id", is(ticket1.getId().toString())))
                .andExpect(jsonPath("$.resObj.ticketRefNo", is(ticket1.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj.title", is(ticket1.getTitle())))
                .andExpect(jsonPath("$.resObj.description", is(ticket1.getDescription())))
                .andExpect(jsonPath("$.resObj.status", is(ticket1.getStatus().toString())));
    }

    @Test
    @DisplayName("test for /tickets/metrics")
    public void get_ticket_metrics_test() throws Exception {
        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets/metrics"), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.resObj.open", is(1)))
                .andExpect(jsonPath("$.resObj.closed", is(0)))
                .andExpect(jsonPath("$.resObj.totalTickets", is(1)));
    }

    @Test
    @DisplayName("test for /tickets/{ticketRefNo}  update")
    public void ticket_update_test() throws Exception {
        Set<String> attachmentsNew = new HashSet<>();
        attachmentsNew.add("D");
        attachmentsNew.add("E");

        TicketUpdateDto dto = new TicketUpdateDto();
        dto.setTitle("Updated ticket title");
        dto.setDescription("Updated description");
        dto.setAttachments(attachmentsNew);

        mvc.perform(utilTestService.setUp(put("/api/v1/client/tickets/"+ticket2.getTicketRefNo()),dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)));
        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        assertEquals(ticketRepo.findById(ticket2.getId()).get().getTitle(),dto.getTitle());

        Ticket ticket3 = new Ticket();
        ticket3.setTitle("Ticket3");
        ticket3.setDescription("Ticket3 description");
        ticket3.setStatus(TicketStatus.OPEN);
        ticket3.setCreatedBy("user3");
        ticket3.setOpenedBy("TestingUser");
        ticket3.setAssignedTo("user5");
        ticketRepo.save(ticket3);

        TicketUpdateDto dtoNew = new TicketUpdateDto();
        dtoNew.setTitle("Updated ticket 3 title");
        dtoNew.setDescription("Updated description of ticket 3");
        dtoNew.setAttachments(attachmentsNew);

        mvc.perform(utilTestService.setUp(put("/api/v1/client/tickets/"+ticket3.getTicketRefNo()),dtoNew, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)));
        verify(emailService, times(2)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        TicketMessage tm3 = new TicketMessage();
        tm3.setMessage("tm3 message");
        tm3.setTicket(ticket2);
        tm3.setMessageType(MessageType.REPLY);
        tm3.setOwner(ticket2.getAssignedTo());
        ticketMessageRepo.save(tm3);

        mvc.perform(utilTestService.setUp(put("/api/v1/client/tickets/"+ticket2.getTicketRefNo()),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("ticket can not be updated"))).andExpect(jsonPath("$.statusCode", is(400)));

        ticket2.setStatus(TicketStatus.CLOSED);
        ticketRepo.save(ticket2);
        mvc.perform(utilTestService.setUp(put("/api/v1/client/tickets/"+ticket2.getTicketRefNo()),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("closed ticket can not be updated"))).andExpect(jsonPath("$.statusCode", is(400)));

    }

    @Test
    @DisplayName("test for /tickets/reply  update")
    public void ticket_update_reply_test() throws Exception {
        Set<String> attachments = new HashSet<>();
        attachments.add("D");
        attachments.add("E");

        TicketUpdateReplyDto dto = new TicketUpdateReplyDto();
        dto.setTicketMessageId(tm1.getId());
        dto.setMessage("updated tm1 message");
        dto.setAttachments(attachments);

        mvc.perform(utilTestService.setUp(put("/api/v1/client/tickets/reply"),dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)));
        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        assertEquals(ticketMessageRepo.findById(tm1.getId()).get().getMessage(),dto.getMessage());
        assertEquals(ticketMessageRepo.findById(tm1.getId()).get().getAttachments().size(),5);

        ticket1.setStatus(TicketStatus.CLOSED);
        ticketRepo.save(ticket1);

        mvc.perform(utilTestService.setUp(put("/api/v1/client/tickets/reply"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("reply can not be updated for closed ticket"))).andExpect(jsonPath("$.statusCode", is(400)));

        TicketMessage tm2 = new TicketMessage();
        tm2.setMessage("tm2 message");
        tm2.setTicket(ticket1);
        tm2.setOwner("TestingUser");
        tm2.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm2);

        ticket1.setStatus(TicketStatus.OPEN);
        ticketRepo.save(ticket1);
        mvc.perform(utilTestService.setUp(put("/api/v1/client/tickets/reply"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("reply can not be updated"))).andExpect(jsonPath("$.statusCode", is(400)));

        TicketUpdateReplyDto dto2 = new TicketUpdateReplyDto();
        dto2.setTicketMessageId(tm2.getId());
        dto2.setMessage("updated tm2 message");
        dto2.setAttachments(attachments);

        mvc.perform(utilTestService.setUp(put("/api/v1/client/tickets/reply"),dto2, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)));
        verify(emailService, times(2)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        assertEquals(ticketMessageRepo.findById(tm2.getId()).get().getMessage(),dto2.getMessage());
    }

    @Test
    @DisplayName("test for ticket events")
    public void ticket_events_test() throws Exception {
        TicketCreateDto ticketCreateDto = new TicketCreateDto();
        Set<String> attachments = new HashSet<>();
        attachments.add("A");

        ticketCreateDto.setApplication(ApplicationEnum.BIZIO_MEET);
        ticketCreateDto.setBrowser(BrowserEnum.GOOGLE_CHROME);
        ticketCreateDto.setTicketType(TicketType.BILLING);
        ticketCreateDto.setOs(OsEnum.PC);
        ticketCreateDto.setTitle("Ticket Event");
        ticketCreateDto.setDescription("This is ticket description for ticket event");
        ticketCreateDto.setDeviceType(DeviceType.MOBILE);
        ticketCreateDto.setAttachments(attachments);
        ticketCreateDto.setOpenedBy("TestingUser");

        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets"),ticketCreateDto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        Ticket ticket = ticketRepo.findByTitle("Ticket Event").orElseThrow(() -> new NotFoundException("ticket not found"));
        ticket.setAssignedTo("admin4");
        ticketRepo.save(ticket);
        TicketStatusChangeDto ticketStatusChangeDto = new TicketStatusChangeDto();
        ticketStatusChangeDto.setTicketRefNo(ticket.getTicketRefNo());
        ticketStatusChangeDto.setStatus("Close");

        //close ticket
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"), ticketStatusChangeDto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(2)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //try to close ticket again
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"), ticketStatusChangeDto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400))).andExpect(jsonPath("$.message", is("ticket is already closed")));

        //reopened ticket
        ticketStatusChangeDto.setStatus("Open");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"), ticketStatusChangeDto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(3)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //try to open ticket again
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"), ticketStatusChangeDto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400))).andExpect(jsonPath("$.message", is("ticket is already open")));

        //close ticket
        ticketStatusChangeDto.setStatus("Close");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"), ticketStatusChangeDto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(4)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //reopened ticket
        ticketStatusChangeDto.setStatus("Open");
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/change-status"), ticketStatusChangeDto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(5)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        mvc.perform(utilTestService.setUp(get("/api/v1/client/tickets/thread/"+ticket.getTicketRefNo()), clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")))
                .andExpect(jsonPath("$.resObj", hasSize(5)))
                .andExpect(jsonPath("$.resObj[0].message", is("TestingUser reopened ticket "+ticket.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[1].message", is("TestingUser closed ticket "+ticket.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[2].message", is("TestingUser reopened ticket "+ticket.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[3].message", is("TestingUser closed ticket "+ticket.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[4].message", is("TestingUser opened ticket "+ticket.getTicketRefNo())));
    }

    @Test
    @DisplayName("test for /tickets/attachments/delete")
    public void delete_ticket_attachments_test() throws Exception {
        Set<String> deleteAttachments = new HashSet<>();
        deleteAttachments.add("B");

        TicketAttachmentsDelete dto = new TicketAttachmentsDelete();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setAttachments(deleteAttachments);

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/client/attachments/delete"),dto)).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/attachments/delete"), dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        Set<String> afterDeleteAttachments = new HashSet<>();
        afterDeleteAttachments.add("A");
        afterDeleteAttachments.add("C");

        assertEquals(afterDeleteAttachments,ticketRepo.findById(ticket1.getId()).get().getAttachments());

        TicketMessage tm2 = new TicketMessage();
        tm2.setMessage("tm2 message");
        tm2.setTicket(ticket1);
        tm2.setOwner(ticket1.getAssignedTo());
        tm2.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm2);

        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/attachments/delete"), dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("ticket's attachments can not be deleted")));

        dto.setTicketRefNo(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/attachments/delete"), dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ticketRefNo", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setAttachments(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/attachments/delete"), dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.attachments", is("must not be null")));
    }

    @Test
    @DisplayName("test for /tickets/reply/attachments/delete")
    public void delete_ticket_reply_attachments_test() throws Exception {
        Set<String> deleteAttachments = new HashSet<>();
        deleteAttachments.add("B");

        TicketReplyAttachmentsDelete dto = new TicketReplyAttachmentsDelete();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setTicketMessageId(tm1.getId());
        dto.setAttachments(deleteAttachments);

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/client/tickets/reply/attachments/delete"),dto)).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/reply/attachments/delete"), dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        Set<String> afterDeleteAttachments = new HashSet<>();
        afterDeleteAttachments.add("A");
        afterDeleteAttachments.add("C");

        assertEquals(afterDeleteAttachments,ticketMessageRepo.findById(tm1.getId()).get().getAttachments());

        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/reply/attachments/delete"), dto, clientDto)).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));


        ticket1.setStatus(TicketStatus.CLOSED);
        ticketRepo.save(ticket1);

        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/reply/attachments/delete"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("reply's attachments can not be deleted for closed ticket"))).andExpect(jsonPath("$.statusCode", is(400)));

        TicketMessage tm2 = new TicketMessage();
        tm2.setMessage("tm2 message");
        tm2.setTicket(ticket1);
        tm2.setOwner("TestingUser");
        tm2.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm2);

        ticket1.setStatus(TicketStatus.OPEN);
        ticketRepo.save(ticket1);
        mvc.perform(utilTestService.setUp(post("/api/v1/client/tickets/reply/attachments/delete"),dto, clientDto)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("reply's attachments can not be deleted"))).andExpect(jsonPath("$.statusCode", is(400)));
    }
}
