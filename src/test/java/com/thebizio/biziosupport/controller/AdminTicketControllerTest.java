package com.thebizio.biziosupport.controller;

import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.*;
import com.thebizio.biziosupport.exception.NotFoundException;
import com.thebizio.biziosupport.repo.TicketMessageRepo;
import com.thebizio.biziosupport.repo.TicketRepo;
import com.thebizio.biziosupport.service.EmailService;
import com.thebizio.biziosupport.service.ExternalApiService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AdminTicketControllerTest extends BaseTestCase {

    @Autowired
    private TicketRepo ticketRepo;

    @MockBean
    private ExternalApiService externalApiService;

    @MockBean
    private EmailService emailService;

    private Ticket ticket1;
    private Ticket ticket2;
    private TicketMessage tm1;
    private TicketMessage tm2;

    private UserDetailsDto userDetailsDto;

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

    @BeforeEach
    public void beforeEach() throws Exception {
        ticketMessageRepo.deleteAll();
        ticketRepo.deleteAll();

        Set<String> attachments = new HashSet<>();
        attachments.add("A");
        attachments.add("B");
        attachments.add("C");

        System.out.println("-----------------");
        System.out.println("TICKET 1 CREATED");
        ticket1 = new Ticket();
        ticket1.setTitle("Ticket1");
        ticket1.setDescription("Ticket1 description");
        ticket1.setStatus(TicketStatus.OPEN);
        ticket1.setAttachments(attachments);
        ticket1.setOpenedBy("user2");
        ticket1.setCreatedBy("user1");
        ticket1.setAssignedTo("user3");
        ticketRepo.save(ticket1);

        System.out.println("-----------------");
        System.out.println("TICKET 2 CREATED");
        ticket2 = new Ticket();
        ticket2.setTitle("Ticket2");
        ticket2.setDescription("Ticket2 description");
        ticket2.setStatus(TicketStatus.OPEN);
        ticket2.setAttachments(attachments);
        ticket2.setCreatedBy("user3");
        ticket2.setOpenedBy("user2");
        ticket2.setAssignedTo("user5");
        ticketRepo.save(ticket2);

        System.out.println("-----------------");
        System.out.println("MESSAGE 1 CREATED");
        tm1 = new TicketMessage();
        tm1.setAttachments(attachments);
        tm1.setMessage("tm1 message");
        tm1.setTicket(ticket1);
        tm1.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm1);

        System.out.println("-----------------");
        System.out.println("MESSAGE 2 CREATED");
        tm2 = new TicketMessage();
        tm2.setAttachments(attachments);
        tm2.setMessage("tm2 message");
        tm2.setOwner("user3");
        tm2.setTicket(ticket1);
        tm2.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm2);

        userDetailsDto = new UserDetailsDto();
        userDetailsDto.setUserName("user2");
        userDetailsDto.setFirstName("tom");
        userDetailsDto.setLastName("cruise");
        userDetailsDto.setEmail("tom@gmail.com");

        when(externalApiService.searchUser(any(String.class),any(boolean.class))).thenReturn(userDetailsDto);
    }

    @Test
    @DisplayName("test for /tickets create")
    public void create_ticket_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");

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

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/admin/tickets"),dto)).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400))).andExpect(jsonPath("$.message", is("openedBy must not be null")));

        dto.setOpenedBy("");

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400))).andExpect(jsonPath("$.message", is("openedBy must not be blank")));

        dto.setOpenedBy("user7");

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        dto.setTitle(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("must not be null or blank")));

        dto.setTitle("");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("must not be null or blank")));
    }

    @Test
    @DisplayName("test for /tickets List")
    public void get_tickets_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");
        mvc.perform(utilTestService.setUpWithoutToken(get("/api/v1/admin/tickets"))).andExpect(status().isUnauthorized());

        //default page number 0 and page size 10
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets"), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(2)))
                .andExpect(jsonPath("$.tickets[1].id", is(ticket1.getId().toString())))
                .andExpect(jsonPath("$.tickets[1].title", is(ticket1.getTitle())))
                .andExpect(jsonPath("$.tickets[1].attachments", is(3)))
                .andExpect(jsonPath("$.tickets[1].conversation", is(2)))
                .andExpect(jsonPath("$.tickets[1].status", is(TicketStatus.OPEN.toString())))
                .andExpect(jsonPath("$.pageSize", is(10)));

        //pass page number 1 and page size 5
        List list = new ArrayList<>();
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?page=1&size=5"), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", is(list)))
                .andExpect(jsonPath("$.pageSize", is(5)));

        //status open filter
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?status=Open"), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(2)));

        //status closed filter
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?status=Closed"), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(0)));

        //ticketRefNo filter
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?ticketRefNo="+ticket1.getTicketRefNo()), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(1)));

        //userName filter
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?userName="+ticket1.getOpenedBy()), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(2)));

        //assignedTo filter
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?assignedTo=user3"), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(1)));

        //status open and UserName filter
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?status=Open&userName="+ticket1.getOpenedBy()), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(2)));

        //status open and assignedTo filter
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?status=Open&assignedTo=user3"), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(2)));

        //status open and assignedTo and UserName filter
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets?status=Open&assignedTo=user3&userName="+ticket1.getOpenedBy()), demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(2)));
    }

    @Test
    @DisplayName("test for /tickets/change-status")
    public void change_ticket_status_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");

        TicketStatusChangeDto dto = new TicketStatusChangeDto();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setStatus("Close");

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/admin/tickets/change-status"),dto)).andExpect(status().isUnauthorized());

        //ticket status is open
        assertEquals(TicketStatus.OPEN,ticketRepo.findById(ticket1.getId()).get().getStatus());

        //close ticket
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        assertEquals(TicketStatus.CLOSED,ticketRepo.findById(ticket1.getId()).get().getStatus());

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //open ticket
        dto.setStatus("Open");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        assertEquals(TicketStatus.OPEN,ticketRepo.findById(ticket1.getId()).get().getStatus());

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        dto.setTicketRefNo(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ticketRefNo", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setStatus(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setStatus("");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("must not be null or blank")));
    }

    @Test
    @DisplayName("test for /tickets/reply")
    public void reply_ticket_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");

        Set<String> attachments = new HashSet<>();
        attachments.add("D");

        TicketReplyDto dto = new TicketReplyDto();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setMessage("This is coming from reply to ticket api");
        dto.setAttachments(attachments);

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/admin/tickets/reply"),dto)).andExpect(status().isUnauthorized());

        //Two messages inside ticket
        assertEquals(2,ticketRepo.findById(ticket1.getId()).get().getMessages().size());
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/reply"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //Three messages inside ticket
        assertEquals(3,ticketRepo.findById(ticket1.getId()).get().getMessages().size());

        dto.setTicketRefNo("");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/reply"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ticketRefNo", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setMessage("");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/reply"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("must not be null or blank")));

        TicketReplyDto dto2 = new TicketReplyDto();
        dto2.setTicketRefNo(ticket2.getTicketRefNo());
        dto2.setMessage("This is coming from reply to ticket api");
        dto2.setAttachments(attachments);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/reply"),dto2, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400))).andExpect(jsonPath("$.message", is("user can not reply to this ticket")));
    }

    @Test
    @DisplayName("test for /tickets/thread/{ticketRefNo}")
    public void get_thread_ticket_test() throws Exception {
        mvc.perform(utilTestService.setUpWithoutToken(get("/api/v1/admin/tickets/thread/"+ticket1.getTicketRefNo()))).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets/thread/"+ticket1.getTicketRefNo()), demoEntityGenerator.getAdminUserDto())).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")))
                .andExpect(jsonPath("$.resObj", hasSize(2)))
                .andExpect(jsonPath("$.resObj[1].id", is(tm1.getId().toString())))
                .andExpect(jsonPath("$.resObj[1].message", is(tm1.getMessage())))
                .andExpect(jsonPath("$.resObj[1].ticketId", is(ticket1.getId().toString())))
                .andExpect(jsonPath("$.resObj[1].ticketRefNo", is(ticket1.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[1].messageType", is(tm1.getMessageType().toString())));
    }

    @Test
    @DisplayName("test for /tickets/assign-ticket")
    public void assign_ticket_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user1");

        TicketAssignDto dto = new TicketAssignDto();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setAdminUserId("adminUser");

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/admin/tickets/assign-ticket"),dto)).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/assign-ticket"),dto, demoEntityGenerator.getAdminUserDto("user1"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        verify(emailService, times(2)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        dto.setTicketRefNo(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/assign-ticket"),dto, demoEntityGenerator.getAdminUserDto("user1"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ticketRefNo", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setAdminUserId("");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/assign-ticket"),dto, demoEntityGenerator.getAdminUserDto("user1"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.adminUserId", is("must not be null or blank")));
    }


    @Test
    @DisplayName("test for /tickets/claim")
    public void claim_ticket_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");
        userDetailsDto.setUserName("user3");
        TicketClaimDto dto = new TicketClaimDto();
        dto.setTicketRefNo(ticket1.getTicketRefNo());

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/admin/tickets/claim"),dto)).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/claim"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        verify(emailService, times(2)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        dto.setTicketRefNo(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/claim"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ticketRefNo", is("must not be null or blank")));
    }

    @Test
    @DisplayName("test for /tickets/{ticketRefNo}")
    public void get_ticket_test() throws Exception {
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets/"+ticket1.getTicketRefNo()), demoEntityGenerator.getAdminUserDto())).andExpect(status().isOk())
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
        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets/metrics"), demoEntityGenerator.getAdminUserDto())).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.resObj.open", is(2)))
                .andExpect(jsonPath("$.resObj.closed", is(0)))
                .andExpect(jsonPath("$.resObj.totalTickets", is(2)));
    }
    @Test
    @DisplayName("test for /tickets/{ticketRefNo}  update")
    public void ticket_update_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");

        Set<String> attachmentsNew = new HashSet<>();
        attachmentsNew.add("D");
        attachmentsNew.add("E");

        TicketUpdateDto dto = new TicketUpdateDto();
        dto.setTitle("Updated ticket title");
        dto.setDescription("Updated description");
        dto.setAttachments(attachmentsNew);

        mvc.perform(utilTestService.setUp(put("/api/v1/admin/tickets/"+ticket2.getTicketRefNo()),dto,demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)));

        assertEquals(ticketRepo.findById(ticket2.getId()).get().getTitle(),dto.getTitle());

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        Ticket ticket3 = new Ticket();
        ticket3.setTitle("Ticket3");
        ticket3.setDescription("Ticket3 description");
        ticket3.setStatus(TicketStatus.OPEN);
        ticket3.setCreatedBy("user3");
        ticket3.setOpenedBy("user2");
        ticket3.setAssignedTo("user5");
        ticketRepo.save(ticket3);

        TicketUpdateDto dtoNew = new TicketUpdateDto();
        dtoNew.setTitle("Updated ticket 3 title");
        dtoNew.setDescription("Updated description of ticket 3");
        dtoNew.setAttachments(attachmentsNew);

        mvc.perform(utilTestService.setUp(put("/api/v1/admin/tickets/"+ticket3.getTicketRefNo()),dtoNew, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)));

        verify(emailService, times(2)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        mvc.perform(utilTestService.setUp(put("/api/v1/admin/tickets/"+ticket1.getTicketRefNo()),dto,demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("user can not update ticket"))).andExpect(jsonPath("$.statusCode", is(400)));

        TicketMessage tm3 = new TicketMessage();
        tm3.setMessage("tm3 message");
        tm3.setTicket(ticket2);
        tm3.setMessageType(MessageType.REPLY);
        tm3.setOwner(ticket2.getAssignedTo());
        ticketMessageRepo.save(tm3);

        mvc.perform(utilTestService.setUp(put("/api/v1/admin/tickets/"+ticket2.getTicketRefNo()),dto,demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("ticket can not be updated"))).andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("test for /tickets/reply  update")
    public void ticket_update_reply_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");

        Set<String> attachments = new HashSet<>();
        attachments.add("D");
        attachments.add("E");

        TicketUpdateReplyDto dto = new TicketUpdateReplyDto();
        dto.setTicketMessageId(tm2.getId());
        dto.setMessage("updated tm2 message");
        dto.setAttachments(attachments);

        mvc.perform(utilTestService.setUp(put("/api/v1/admin/tickets/reply"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OK"))).andExpect(jsonPath("$.statusCode", is(200)));

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        assertEquals(ticketMessageRepo.findById(tm2.getId()).get().getMessage(),dto.getMessage());
        assertEquals(ticketMessageRepo.findById(tm2.getId()).get().getAttachments().size(),5);

        TicketMessage tm3 = new TicketMessage();
        tm3.setMessage("tm3 message");
        tm3.setTicket(ticket1);
        tm3.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm3);

        mvc.perform(utilTestService.setUp(put("/api/v1/admin/tickets/reply"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("reply can not be updated"))).andExpect(jsonPath("$.statusCode", is(400)));
    }


    @Test
    @DisplayName("test for ticket events")
    public void ticket_events_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");

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
        ticketCreateDto.setOpenedBy("user3");

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets"),ticketCreateDto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        Ticket ticket = ticketRepo.findByTitle("Ticket Event").orElseThrow(() -> new NotFoundException("ticket not found"));

        verify(emailService, times(1)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        userDetailsDto.setUserName("adminUser");
        TicketAssignDto ticketAssignDto = new TicketAssignDto();
        ticketAssignDto.setTicketRefNo(ticket.getTicketRefNo());
        ticketAssignDto.setAdminUserId("adminUser");

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/assign-ticket"), ticketAssignDto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(3)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        userDetailsDto.setUserName("adminUser2");
        ticketAssignDto.setAdminUserId("adminUser2");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/assign-ticket"), ticketAssignDto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(5)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        userDetailsDto.setUserName("admin_user3");
        ticketAssignDto.setAdminUserId("admin_user3");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/assign-ticket"), ticketAssignDto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(7)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

//        when(utilService.getAuthUserName()).thenReturn("admin_user3");

        TicketStatusChangeDto ticketStatusChangeDto = new TicketStatusChangeDto();
        ticketStatusChangeDto.setTicketRefNo(ticket.getTicketRefNo());
        ticketStatusChangeDto.setStatus("Close");

        //close ticket
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"), ticketStatusChangeDto, demoEntityGenerator.getAdminUserDto("admin_user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(8)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //try to close ticket again
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"), ticketStatusChangeDto, demoEntityGenerator.getAdminUserDto("admin_user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400))).andExpect(jsonPath("$.message", is("ticket is already closed")));

        //reopened ticket
        ticketStatusChangeDto.setStatus("Open");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"), ticketStatusChangeDto, demoEntityGenerator.getAdminUserDto("admin_user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(9)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //try to open ticket again
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"), ticketStatusChangeDto, demoEntityGenerator.getAdminUserDto("admin_user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400))).andExpect(jsonPath("$.message", is("ticket is already open")));

        //close ticket
        ticketStatusChangeDto.setStatus("Close");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"), ticketStatusChangeDto, demoEntityGenerator.getAdminUserDto("admin_user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(10)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        //reopened ticket
        ticketStatusChangeDto.setStatus("Open");
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/change-status"), ticketStatusChangeDto, demoEntityGenerator.getAdminUserDto("admin_user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));
        verify(emailService, times(11)).sendMailMimeWithHtml(anyString(), anyString(), any(Map.class), anyString());

        mvc.perform(utilTestService.setUp(get("/api/v1/admin/tickets/thread/"+ticket.getTicketRefNo()), demoEntityGenerator.getAdminUserDto("admin_user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")))
                .andExpect(jsonPath("$.resObj", hasSize(8)))
                .andExpect(jsonPath("$.resObj[0].message", is("admin_user3 reopened ticket "+ticket.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[1].message", is("admin_user3 closed ticket "+ticket.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[2].message", is("admin_user3 reopened ticket "+ticket.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[3].message", is("admin_user3 closed ticket "+ticket.getTicketRefNo())))
                .andExpect(jsonPath("$.resObj[4].message", is(ticket.getTicketRefNo()+" is reassigned to admin_user3")))
                .andExpect(jsonPath("$.resObj[5].message", is(ticket.getTicketRefNo()+" is reassigned to adminUser2")))
                .andExpect(jsonPath("$.resObj[6].message", is(ticket.getTicketRefNo()+" is assigned to adminUser")))
                .andExpect(jsonPath("$.resObj[7].message", is("user3 opened ticket "+ticket.getTicketRefNo())));

    }

    @Test
    @DisplayName("test for /tickets/attachments/delete")
    public void delete_ticket_attachments_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user1");
        Set<String> deleteAttachments = new HashSet<>();
        deleteAttachments.add("B");

        TicketAttachmentsDelete dto = new TicketAttachmentsDelete();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setAttachments(deleteAttachments);

        ticketMessageRepo.deleteAll();

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/admin/attachments/delete"),dto)).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/attachments/delete"), dto, demoEntityGenerator.getAdminUserDto("user1"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        Set<String> afterDeleteAttachments = new HashSet<>();
        afterDeleteAttachments.add("A");
        afterDeleteAttachments.add("C");

        assertEquals(afterDeleteAttachments,ticketRepo.findById(ticket1.getId()).get().getAttachments());

        TicketMessage tm3 = new TicketMessage();
        tm3.setMessage("tm3 message");
        tm3.setTicket(ticket1);
        tm3.setOwner(ticket1.getAssignedTo());
        tm3.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm3);

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/attachments/delete"), dto, demoEntityGenerator.getAdminUserDto("user1"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("ticket's attachments can not be deleted")));

        dto.setTicketRefNo(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/attachments/delete"), dto, demoEntityGenerator.getAdminUserDto("user1"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ticketRefNo", is("must not be null or blank")));

        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setAttachments(null);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/attachments/delete"), dto, demoEntityGenerator.getAdminUserDto("user1"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.attachments", is("must not be null")));
    }

    @Test
    @DisplayName("test for /tickets/reply/attachments/delete")
    public void delete_ticket_reply_attachments_test() throws Exception {
//        when(utilService.getAuthUserName()).thenReturn("user3");
        Set<String> deleteAttachments = new HashSet<>();
        deleteAttachments.add("B");

        TicketReplyAttachmentsDelete dto = new TicketReplyAttachmentsDelete();
        dto.setTicketRefNo(ticket1.getTicketRefNo());
        dto.setTicketMessageId(tm2.getId());
        dto.setAttachments(deleteAttachments);

        mvc.perform(utilTestService.setUpWithoutToken(post("/api/v1/admin/tickets/reply/attachments/delete"),dto)).andExpect(status().isUnauthorized());

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/reply/attachments/delete"), dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));

        Set<String> afterDeleteAttachments = new HashSet<>();
        afterDeleteAttachments.add("A");
        afterDeleteAttachments.add("C");

        assertEquals(afterDeleteAttachments,ticketMessageRepo.findById(tm2.getId()).get().getAttachments());

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/reply/attachments/delete"), dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200))).andExpect(jsonPath("$.message", is("OK")));


        ticket1.setStatus(TicketStatus.CLOSED);
        ticketRepo.save(ticket1);

        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/reply/attachments/delete"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("reply's attachments can not be deleted for closed ticket"))).andExpect(jsonPath("$.statusCode", is(400)));

        TicketMessage tm3 = new TicketMessage();
        tm3.setMessage("tm3 message");
        tm3.setTicket(ticket1);
        tm3.setOwner("TestingUser");
        tm3.setMessageType(MessageType.REPLY);
        ticketMessageRepo.save(tm3);

        ticket1.setStatus(TicketStatus.OPEN);
        ticketRepo.save(ticket1);
        mvc.perform(utilTestService.setUp(post("/api/v1/admin/tickets/reply/attachments/delete"),dto, demoEntityGenerator.getAdminUserDto("user3"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("reply's attachments can not be deleted"))).andExpect(jsonPath("$.statusCode", is(400)));
    }

}