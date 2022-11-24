package com.thebizio.biziosupport.service;

import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.MessageType;
import com.thebizio.biziosupport.enums.TicketStatus;
import com.thebizio.biziosupport.enums.TicketType;
import com.thebizio.biziosupport.exception.AlreadyExistsException;
import com.thebizio.biziosupport.exception.NotFoundException;
import com.thebizio.biziosupport.repo.TicketMessageRepo;
import com.thebizio.biziosupport.repo.TicketRepo;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TicketService {

    @Value(("${enable-notifications}"))
    private String ENABLE_NOTIFICATIONS;

    @Autowired
    private UtilService utilService;

    @Autowired
    private TicketRepo ticketRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ExternalApiService externalApiService;

    @Autowired
    private TicketMessageService ticketMessageService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TicketMessageRepo ticketMessageRepo;
    public Ticket findById(UUID id){
        return ticketRepo.findById(id).orElseThrow(() -> new NotFoundException("ticket not found"));
    }

    public Ticket findByTicketRefNo(String ticketRefNo){
        return ticketRepo.findByTicketRefNo(ticketRefNo).orElseThrow(() -> new NotFoundException("ticket ref no. not found"));
    }

    @Transactional
    public String createTicket(TicketCreateDto dto,boolean adminUser) {
        Ticket ticket = new Ticket();
        ticket.setTicketType(dto.getTicketType());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setDeviceType(dto.getDeviceType());
        ticket.setOs(dto.getOs());
        ticket.setApplication(dto.getApplication());
        if (dto.getIssueOnWebBrowser() == true && dto.getBrowser() == null && dto.getTicketType() == TicketType.TECHNICAL) {
            throw new NotFoundException("browser must not be null");
        }
        ticket.setIssueOnWebBrowser(dto.getIssueOnWebBrowser());
        ticket.setBrowser(dto.getBrowser());
        ticket.setAttachments(dto.getAttachments());
        ticket.setOsVersion(dto.getOsVersion());
        ticket.setApplicationVersion(dto.getApplicationVersion());
        ticket.setBrowserVersion(dto.getBrowserVersion());
        ticket.setStatus(TicketStatus.OPEN);
        if (adminUser){
            if (dto.getOpenedBy() != null) {
                if ( !dto.getOpenedBy().isEmpty()){
                    ticket.setOpenedBy(dto.getOpenedBy());
                }else{
                    throw new NotFoundException("openedBy must not be blank");
                }
            }else{
                throw new NotFoundException("openedBy must not be null");
            }
        }else {
            ticket.setOpenedBy(utilService.getAuthUserName());
        }

        ticketRepo.save(ticket);
        ticketRepo.flush();

        TicketMessage ticketMessage = new TicketMessage();
        ticketMessage.setMessage(ticket.getOpenedBy()+" opened ticket "+ticket.getTicketRefNo());
        ticketMessage.setMessageType(MessageType.EVENT);
        ticketMessage.setTicket(ticket);
        ticketMessageRepo.save(ticketMessage);

        if (!ticket.getCreatedBy().equals(ticket.getOpenedBy())){
            sendEmailToUser(ticket.getOpenedBy(),ticket,"Ticket Created","ticket-created-notification-to-user.ftl");
        }
        return "OK";
    }

    public List<TicketDto> mapTicketEntityToDto(Page<Ticket> tickets) {
        List<TicketDto> ticketDtoList =new ArrayList<>();

        for (Ticket ticket : tickets.getContent()) {
            TicketDto dto = new TicketDto();
            dto.setId(ticket.getId());
            dto.setAttachments(ticket.getAttachments().size());
            dto.setTitle(ticket.getTitle());
            dto.setStatus(ticket.getStatus());
            dto.setConversation(ticketMessageRepo.countTicketMessageByTicketAndMessageType(ticket,MessageType.REPLY));
            dto.setType(ticket.getTicketType());
            dto.setTicketRefNo(ticket.getTicketRefNo());
            dto.setCreatedBy(ticket.getCreatedBy());
            dto.setModifiedBy(ticket.getModifiedBy());
            dto.setCreatedDate(ticket.getCreatedDate());
            dto.setLastModifiedDate(ticket.getLastModifiedDate());
            dto.setOpenedBy(ticket.getOpenedBy());
            dto.setAssignedTo(ticket.getAssignedTo());
            ticketDtoList.add(dto);
        }
        return ticketDtoList;
    }

    public TicketPaginationDto mapObjectToPagination(List<TicketDto> tickets, Integer pageSize, Integer totalPages){
        TicketPaginationDto dto = new TicketPaginationDto();
        dto.setTickets(tickets);
        dto.setTotalPages(totalPages);
        dto.setPageSize(pageSize);
        return dto;
    }

    public TicketPaginationDto getAllTicket(Optional<Integer> page, Optional<Integer> size,boolean adminUser,
                                            Optional<String> ticketRefNo,Optional<String> status,Optional<String> userName,
                                            Optional<String> assignedTo) {

        Pageable paging = PageRequest.of(page.orElse(0),size.orElse(10));
        Page<Ticket> tickets = null;

        if(adminUser){
            tickets = ticketRepo.findAllByOrderByCreatedDateDesc(paging);

            if (ticketRefNo.isPresent()){
                Optional<Ticket> t = ticketRepo.findByTicketRefNo(ticketRefNo.get());
                tickets = addTicketToPage(t,tickets);
            } else if (status.isPresent()) {
                tickets = ticketRepo.findByStatusOrderByCreatedDateDesc(paging,getTicketOrderStatusEnum(status.get()));
            } else if (userName.isPresent()) {
                tickets = ticketRepo.findByOpenedByOrderByCreatedDateDesc(paging,userName.get());
            } else if (assignedTo.isPresent()) {
                tickets = ticketRepo.findByAssignedToOrderByCreatedDateDesc(paging,assignedTo.get());
            }else if (userName.isPresent() && status.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndStatusOrderByCreatedDateDesc(paging,userName.get(),getTicketOrderStatusEnum(status.get()));
            }else if (assignedTo.isPresent() && status.isPresent()) {
                tickets = ticketRepo.findByAssignedToAndStatusOrderByCreatedDateDesc(paging,assignedTo.get(),getTicketOrderStatusEnum(status.get()));
            }else if (userName.isPresent() && assignedTo.isPresent() && status.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndAssignedToAndStatusOrderByCreatedDateDesc(paging,userName.get(),assignedTo.get(),getTicketOrderStatusEnum(status.get()));
            }

        } else{
            String loggedUserName = utilService.getAuthUserName();
            tickets = ticketRepo.findByOpenedByOrderByCreatedDateDesc(paging,loggedUserName);
            if (ticketRefNo.isPresent()){
                Optional<Ticket> t = ticketRepo.findByOpenedByAndTicketRefNo(loggedUserName,ticketRefNo.get());
                tickets = addTicketToPage(t,tickets);
            } else if (status.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndStatusOrderByCreatedDateDesc(paging,loggedUserName,getTicketOrderStatusEnum(status.get()));
            } else if (assignedTo.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndAssignedToOrderByCreatedDateDesc(paging,loggedUserName,assignedTo.get());
            }else if (assignedTo.isPresent() && status.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndAssignedToAndStatusOrderByCreatedDateDesc(paging,loggedUserName,assignedTo.get(),getTicketOrderStatusEnum(status.get()));
            }
        }
        return mapObjectToPagination(mapTicketEntityToDto(tickets),tickets.getSize(),
                tickets.getTotalPages());
    }

    public Page<Ticket> addTicketToPage(Optional<Ticket> t, Page<Ticket> tickets){
        List<Ticket> ticketList = new ArrayList<>();
        if (t.isPresent()){
            ticketList.add(t.get());
            tickets = new PageImpl<>(ticketList);
            return tickets;
        }else {
            tickets = new PageImpl<>(ticketList);
            return tickets;
        }
    }

    public TicketMetricsDto setTicketCounts(Long ticketsCount, List<TicketStatusMetricsDto> ticketsStatusCount){
        TicketMetricsDto dto = new TicketMetricsDto();
        for (TicketStatusMetricsDto tsm:ticketsStatusCount){
            if (tsm.getStatus().equals(TicketStatus.OPEN)){
                dto.setOpen(tsm.getCount());
            } else if (tsm.getStatus().equals(TicketStatus.CLOSED)) {
                dto.setClosed(tsm.getCount());
            }
        }
        dto.setTotalTickets(ticketsCount);
        return dto;
    }

    public TicketStatus getTicketOrderStatusEnum(String status){
        if (status.equals("Open")){
            return TicketStatus.OPEN;
        } else if (status.equals("Closed")) {
            return TicketStatus.CLOSED;
        }else {
            throw new NotFoundException("ticket status not found");
        }
    }


    @Transactional
    public String changeTicketStatus(TicketStatusChangeDto dto,boolean adminUser) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        String userName = utilService.getAuthUserName();
        if(adminUser) {
            if (ticket.getAssignedTo() == null || ticket.getAssignedTo().isEmpty()) {
                if(ticket.getCreatedBy().equals(userName)){
                    changeStatus(dto, ticket, userName);
                    return "OK";
                }else {
                    throw new NotFoundException("user can not change the ticket status");
                }
            } else {
                if(ticket.getAssignedTo().equals(userName)){
                    changeStatus(dto, ticket, userName);
                    return "OK";
                }else {
                    throw new NotFoundException("user can not change the ticket status");
                }
            }
        }else {
            if (ticket.getOpenedBy().equals(userName)){
                changeStatus(dto,ticket,userName);
                return "OK";
            }else {
                throw new NotFoundException("user can not change the ticket status");
            }
        }
    }

    public void createTicketMessage(Ticket ticket,TicketStatus status,String userName){
        if (status.equals(TicketStatus.OPEN)) {
            List<TicketMessage> ticketMessageFound = ticketMessageRepo.findAllByTicketAndMessageTypeAndMessageLike(ticket,MessageType.EVENT,"%opened ticket%");
            TicketMessage ticketMessage = new TicketMessage();
            if (ticketMessageFound.size() > 0){
                ticketMessage.setMessage(userName+" reopened ticket "+ticket.getTicketRefNo());
                sendTicketReOpenedNotification(ticket,userName);
            }else {
                ticketMessage.setMessage(userName+" opened ticket "+ticket.getTicketRefNo());
            }
            ticketMessage.setTicket(ticket);
            ticketMessage.setMessageType(MessageType.EVENT);
            ticketMessageRepo.save(ticketMessage);
        } else if (status.equals(TicketStatus.CLOSED)){
            TicketMessage ticketMessage = new TicketMessage();
            ticketMessage.setMessage(userName+" closed ticket "+ticket.getTicketRefNo());
            ticketMessage.setMessageType(MessageType.EVENT);
            ticketMessage.setTicket(ticket);
            ticketMessageRepo.save(ticketMessage);
        }
    }

    public void changeStatus(TicketStatusChangeDto dto,Ticket ticket,String userName) {
        if (dto.getStatus().equals("Open")) {
            if (ticket.getStatus().equals(TicketStatus.OPEN)){
                throw new AlreadyExistsException("ticket is already open");
            }else {
                ticket.setStatus(TicketStatus.OPEN);
//                ticket.setOpenedBy(userName);
                ticketRepo.save(ticket);
                createTicketMessage(ticket, TicketStatus.OPEN,userName);
            }
        } else if (dto.getStatus().equals("Close")) {
            if (ticket.getStatus().equals(TicketStatus.CLOSED)){
                throw new AlreadyExistsException("ticket is already closed");
            }else {
                ticket.setStatus(TicketStatus.CLOSED);
                ticket.setClosedBy(userName);
                ticketRepo.save(ticket);
                createTicketMessage(ticket, TicketStatus.CLOSED,userName);
                sendTicketClosedNotification(ticket);
            }
        } else {
            throw new NotFoundException("status should be Open or Close");
        }
    }

    public String getAssignedTo(Ticket ticket){
        String assignedTo;
        if (ticket.getAssignedTo() == null){
            assignedTo = "";
        }else {
            assignedTo = ticket.getAssignedTo();
        }
        return assignedTo;
    }

    public void sendTicketClosedNotification(Ticket ticket){
        String assignedTo = getAssignedTo(ticket);
        if (!ticket.getCreatedBy().equals(ticket.getOpenedBy()) && ticket.getCreatedBy().equals(ticket.getClosedBy()) && !ticket.getCreatedBy().equals(assignedTo)){
            sendEmailToUser(ticket.getOpenedBy(),ticket,"Ticket Closed","ticket-closed-by-bizio-admin-notification-to-user.ftl");
            if (!assignedTo.isEmpty()){
                sendEmailToAdmin(ticket.getAssignedTo(),ticket,"Ticket Closed","ticket-closed-notification-to-admin.ftl");
            }
        } else if (ticket.getOpenedBy().equals(ticket.getClosedBy())) {
            if (!assignedTo.isEmpty()){
                sendEmailToAdmin(ticket.getAssignedTo(),ticket,"Ticket Closed","ticket-closed-notification-to-admin.ftl");
            }
        } else if (assignedTo.equals(ticket.getClosedBy())) {
            sendEmailToUser(ticket.getOpenedBy(),ticket,"Ticket Closed","ticket-closed-notification-to-user.ftl");
        }
    }

    public void sendTicketReOpenedNotification(Ticket ticket, String username){
        String assignedTo = getAssignedTo(ticket);
        if (ticket.getCreatedBy().equals(username) && !ticket.getCreatedBy().equals(ticket.getOpenedBy()) && !ticket.getCreatedBy().equals(assignedTo)){
            sendEmailToUser(ticket.getOpenedBy(),ticket,"Ticket ReOpened","ticket-reopened-by-bizio-admin-notification-to-user.ftl");
            if (!assignedTo.isEmpty()){
                sendEmailToAdmin(ticket.getAssignedTo(),ticket,"Ticket ReOpened","ticket-reopened-notification-to-admin.ftl");
            }
        } else if (ticket.getOpenedBy().equals(username)) {
            if (!assignedTo.isEmpty()){
                sendEmailToAdmin(ticket.getAssignedTo(),ticket,"Ticket ReOpened","ticket-reopened-notification-to-admin.ftl");
            }
        } else if (assignedTo.equals(username)) {
            sendEmailToUser(ticket.getOpenedBy(),ticket,"Ticket ReOpened","ticket-reopened-notification-to-user.ftl");
        }
    }

    public void sendEmailToAdmin(String userName,Ticket ticket,String subject, String template){
        UserDetailsDto adminUser = externalApiService.searchUser(userName,true);
        sendSuccessMail(adminUser.getEmail(),subject,template,
                adminUser.getFirstName(),adminUser.getLastName(),ticket.getOpenedBy(),adminUser.getUserName(),ticket.getTicketRefNo());
    }

    public void sendEmailToUser(String userName,Ticket ticket,String subject, String template){
        UserDetailsDto user = externalApiService.searchUser(userName,false);
        sendSuccessMail(user.getEmail(),subject,template,
                user.getFirstName(),user.getLastName(),user.getUserName(),ticket.getAssignedTo(),ticket.getTicketRefNo());
    }

    public String replyTicket(TicketReplyDto dto) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        String userName = utilService.getAuthUserName();
        String ticketAssignedTo;
        if(ticket.getAssignedTo() == null){
            ticketAssignedTo = "";
        }else {
            ticketAssignedTo = ticket.getAssignedTo();
        }

        if (ticketAssignedTo.equals(userName) || ticket.getOpenedBy().equals(userName)) {
            TicketMessage tm = new TicketMessage();
            tm.setMessage(dto.getMessage());
            tm.setAttachments(dto.getAttachments());
            tm.setOwner(userName);
            tm.setTicket(ticket);
            tm.setMessageType(MessageType.REPLY);
            ticketMessageRepo.save(tm);

            if (userName.equals(ticketAssignedTo)){
                sendEmailToUser(ticket.getOpenedBy(),ticket,"Comment Posted","ticket-reply-notification-to-user.ftl");
            } else if (userName.equals(ticket.getOpenedBy()) && !ticketAssignedTo.isEmpty()) {
                sendEmailToAdmin(ticket.getAssignedTo(),ticket,"Comment Posted","ticket-reply-notification-to-admin.ftl");
            }
            return "OK";
        } else {
            throw new NotFoundException("user can not reply to this ticket");
        }
    }

    public List<TicketMessageDto> getThreadTicket(String ticketRefNo,boolean adminUser) {
        Ticket ticket = findByTicketRefNo(ticketRefNo);
        if (adminUser || ticket.getOpenedBy().equals(utilService.getAuthUserName())) {
            List<TicketMessage> tmList = ticketMessageRepo.findAllByTicketTicketRefNoOrderByCreatedDateDesc(ticketRefNo);
            return modelMapper.map(tmList, new TypeToken<List<TicketMessageDto>>() {}.getType());
        }else {
            throw new NotFoundException("user can not read the messages");
        }
    }

    @Transactional
    public String assignTicket(TicketAssignDto dto) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        UserDetailsDto adminUser = externalApiService.searchUser(dto.getAdminUserId(),true);
        ticket.setAssignedTo(adminUser.getUserName());
        ticketRepo.save(ticket);
        ticketMessageService.createAssignedToTicketMessageEvent(ticket);

        sendSuccessMail(adminUser.getEmail(),"Ticket Claimed","ticket-assigned-notification-to-admin.ftl",
                adminUser.getFirstName(),adminUser.getLastName(),adminUser.getUserName(),ticket.getCreatedBy(),ticket.getTicketRefNo());

        sendEmailToUser(ticket.getOpenedBy(),ticket,"Ticket Claimed","ticket-assigned-notification-to-user.ftl");
        return "OK";
    }

    public TicketDetailsDto getTicket(String ticketRefNo,boolean adminUser) {
        Ticket ticket = findByTicketRefNo(ticketRefNo);
        if (adminUser || ticket.getOpenedBy().equals(utilService.getAuthUserName())) {
            return modelMapper.map(ticket, TicketDetailsDto.class);
        }else {
            throw new NotFoundException("user can not read the ticket details");
        }
    }

    public Object getTicketMetrics(boolean adminUser) {
        if(adminUser){
            Long ticketsCount = ticketRepo.countTickets();
            List<TicketStatusMetricsDto>  ticketsStatusCount = ticketRepo.countTicketByStatus();
            return setTicketCounts(ticketsCount,ticketsStatusCount);
        } else{
            String loggedUserName = utilService.getAuthUserName();
            Long ticketsCount = ticketRepo.countTicketsByOpenedBy(loggedUserName);
            List<TicketStatusMetricsDto>  ticketsStatusCount = ticketRepo.countTicketByStatusAndOpenedBy(loggedUserName);
            return setTicketCounts(ticketsCount,ticketsStatusCount);
        }
    }

    public String updateTicket(String ticketRefNo,TicketUpdateDto dto) {
        Ticket ticket = findByTicketRefNo(ticketRefNo);
        String userName = utilService.getAuthUserName();
        if (ticket.getCreatedBy().equals(userName) || ticket.getOpenedBy().equals(userName)) {
            if (ticket.getStatus().equals(TicketStatus.OPEN)) {
                if (ticketMessageRepo.findAllByTicketAndMessageTypeAndOwner(ticket,MessageType.REPLY,ticket.getAssignedTo()).size() == 0) {
                    Boolean sentEmail = false;
                    ticket.setTitle(dto.getTitle());

                    if (ticket.getDescription() != dto.getDescription()){
                        ticket.setDescription(dto.getDescription());
                        sentEmail = true;
                    }
                    ticket.setTicketType(dto.getTicketType());
                    ticket.setDeviceType(dto.getDeviceType());
                    ticket.setOs(dto.getOs());
                    ticket.setApplication(dto.getApplication());
                    ticket.setBrowser(dto.getBrowser());
                    ticket.setOsVersion(dto.getOsVersion());
                    ticket.setApplicationVersion(dto.getApplicationVersion());
                    ticket.setBrowserVersion(dto.getBrowserVersion());
                    if (dto.getAttachments().size() > 0) {
                        Set<String> attachments = ticket.getAttachments();
                        if (attachments.isEmpty()) {
                            ticket.setAttachments(dto.getAttachments());
                        } else {
                            for (String s : dto.getAttachments()) {
                                attachments.add(s);
                            }
                            ticket.setAttachments(attachments);
                        }
                    }
                    ticketRepo.save(ticket);

                    if (sentEmail){
                        if (userName.equals(ticket.getCreatedBy())){
                            sendEmailToUser(ticket.getOpenedBy(),ticket,"Ticket Updated","ticket-update-notification-to-user.ftl");
                        } else if (userName.equals(ticket.getOpenedBy())) {
                            sendEmailToAdmin(ticket.getCreatedBy(),ticket,"Ticket Updated","ticket-update-notification-to-admin.ftl");
                        }
                    }
                    return "OK";
                }else {
                    throw new AlreadyExistsException("ticket can not be updated");
                }
            } else {
                throw new AlreadyExistsException("closed ticket can not be updated");
            }
        }else {
            throw new NotFoundException("user can not update ticket");
        }
    }

    public String updateTicketReply(TicketUpdateReplyDto dto) {
        TicketMessage ticketMessage = ticketMessageRepo.findById(dto.getTicketMessageId()).orElseThrow(() -> new NotFoundException("ticket message id not found"));
        if (ticketMessage.getOwner().equals(utilService.getAuthUserName())) {
            Optional<TicketMessage> latestTicketMessage = null;
            if (ticketMessage.getTicket().getStatus().equals(TicketStatus.OPEN)) {
                if (dto.getTicketRefNo() == null || dto.getTicketRefNo().isEmpty()) {
                    latestTicketMessage = ticketMessageRepo.findFirst1ByTicketTicketRefNoAndMessageTypeOrderByCreatedDateDesc(ticketMessage.getTicket().getTicketRefNo(),MessageType.REPLY);
                } else {
                    latestTicketMessage = ticketMessageRepo.findFirst1ByTicketTicketRefNoAndMessageTypeOrderByCreatedDateDesc(dto.getTicketRefNo(),MessageType.REPLY);
                }

                if (!latestTicketMessage.isPresent()){
                    throw new NotFoundException("last ticket message not found");
                }

                if (ticketMessage.getId() == latestTicketMessage.get().getId()) {
                    ticketMessage.setMessage(dto.getMessage());

                    if (dto.getAttachments().size() > 0) {
                        Set<String> attachments = ticketMessage.getAttachments();
                        if (attachments.isEmpty()) {
                            ticketMessage.setAttachments(dto.getAttachments());
                        } else {
                            for (String s : dto.getAttachments()) {
                                attachments.add(s);
                            }
                            ticketMessage.setAttachments(attachments);
                        }
                    }
                    ticketMessageRepo.save(ticketMessage);
                    Ticket ticket = ticketMessage.getTicket();
                    if(utilService.getAuthUserName().equals(ticket.getAssignedTo())){
                        sendEmailToUser(ticket.getOpenedBy(),ticket,"Comment Edited","ticket-reply-edit-notification-to-user.ftl");
                    } else if (utilService.getAuthUserName().equals(ticket.getOpenedBy())) {
                        sendEmailToAdmin(ticket.getAssignedTo(),ticket,"Comment Edited","ticket-reply-edit-notification-to-admin.ftl");
                    }
                    return "OK";
                } else {
                    throw new AlreadyExistsException("reply can not be updated");
                }
            } else {
                throw new AlreadyExistsException("reply can not be updated for closed ticket");
            }
        }else {
            throw new NotFoundException("user can not update ticket message");
        }
    }

    @Transactional
    public String claimTicket(TicketClaimDto dto) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        String userName = utilService.getAuthUserName();
        ticket.setAssignedTo(externalApiService.searchUser(userName,true).getUserName());
        ticketRepo.save(ticket);
        ticketMessageService.createAssignedToTicketMessageEvent(ticket);

        sendEmailToUser(ticket.getOpenedBy(),ticket,"Ticket Claimed","ticket-assigned-notification-to-user.ftl");
        return "OK";
    }

    public String deleteTicketAttachments(TicketAttachmentsDelete dto) {
        String userName = utilService.getAuthUserName();
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        if (ticket.getCreatedBy().equals(userName) || ticket.getOpenedBy().equals(userName)) {
            if (ticket.getStatus().equals(TicketStatus.OPEN)) {
                if (ticketMessageRepo.findAllByTicketAndMessageTypeAndOwner(ticket,MessageType.REPLY,ticket.getAssignedTo()).size() == 0) {
                    for (String s : dto.getAttachments()) {
                        ticket.getAttachments().remove(s);
                    }
                    ticketRepo.save(ticket);
                    return "OK";
                }else {
                    throw new AlreadyExistsException("ticket's attachments can not be deleted");
                }
            }else {
                throw new AlreadyExistsException("can not delete attachments of closed ticket");
            }
        }else {
            throw new AlreadyExistsException("user can not delete attachments");
        }
    }

    public String deleteTicketReplyAttachments(TicketReplyAttachmentsDelete dto) {
        TicketMessage ticketMessage = ticketMessageRepo.findById(dto.getTicketMessageId()).orElseThrow(() -> new NotFoundException("ticket message id not found"));
        if (ticketMessage.getOwner().equals(utilService.getAuthUserName())) {
            Optional<TicketMessage> latestTicketMessage = null;
            if (ticketMessage.getTicket().getStatus().equals(TicketStatus.OPEN)) {
                if (dto.getTicketRefNo() == null || dto.getTicketRefNo().isEmpty()) {
                    latestTicketMessage = ticketMessageRepo.findFirst1ByTicketTicketRefNoAndMessageTypeOrderByCreatedDateDesc(ticketMessage.getTicket().getTicketRefNo(),MessageType.REPLY);
                } else {
                    latestTicketMessage = ticketMessageRepo.findFirst1ByTicketTicketRefNoAndMessageTypeOrderByCreatedDateDesc(dto.getTicketRefNo(),MessageType.REPLY);
                }

                if (!latestTicketMessage.isPresent()){
                    throw new NotFoundException("last ticket message not found");
                }

                if (ticketMessage.getId() == latestTicketMessage.get().getId()) {
                    for (String s : dto.getAttachments()) {
                        ticketMessage.getAttachments().remove(s);
                    }
                    ticketMessageRepo.save(ticketMessage);
                    return "OK";
                } else {
                    throw new AlreadyExistsException("reply's attachments can not be deleted");
                }
            } else {
                throw new AlreadyExistsException("reply's attachments can not be deleted for closed ticket");
            }
        }else {
            throw new NotFoundException("user can not delete reply's attachments");
        }
    }

    public boolean sendSuccessMail(String email, String subject, String templateName,
                                   String firstName,String LastName, String userName,
                                   String adminUserName, String ticketRefNo) {
        String recipientAddress = email;
        Map<String, Object> map = new HashMap<>();
        map.put("firstName", firstName);
        map.put("lastName", LastName);
        map.put("adminUserName", adminUserName);
        map.put("userName", userName);
        map.put("ticketRefNo", ticketRefNo);

        if(ENABLE_NOTIFICATIONS.equals("true")){
            emailService.sendMailMimeWithHtml(recipientAddress, subject, map, templateName);
        }
        return true;
    }
}
