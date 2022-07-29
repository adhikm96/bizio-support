package com.thebizio.biziosupport.service;

import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.TicketStatus;
import com.thebizio.biziosupport.exception.AlreadyExistsException;
import com.thebizio.biziosupport.exception.NotFoundException;
import com.thebizio.biziosupport.repo.TicketMessageRepo;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.thebizio.biziosupport.repo.TicketRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private UtilService utilService;

    @Autowired
    private TicketRepo ticketRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TicketMessageRepo ticketMessageRepo;
    public Ticket findById(UUID id){
        return ticketRepo.findById(id).orElseThrow(() -> new NotFoundException("ticket not found"));
    }

    public Ticket findByTicketRefNo(String ticketRefNo){
        return ticketRepo.findByTicketRefNo(ticketRefNo).orElseThrow(() -> new NotFoundException("ticket ref no. not found"));
    }

    public String createTicket(TicketCreateDto dto,boolean adminUser) {
        Ticket ticket = new Ticket();
        ticket.setTicketType(dto.getTicketType());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setDeviceType(dto.getDeviceType());
        ticket.setOs(dto.getOs());
        ticket.setApplication(dto.getApplication());
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

        return "OK";
    }

    public List<TicketDto> mapTicketEntityToDto(Page<Ticket> tickets) {
        List<TicketDto> ticketDtoList =new ArrayList<>();

        for (Ticket ticket : tickets.getContent()) {
            TicketDto dto = new TicketDto();
            dto.setId(ticket.getId());
            dto.setAttachments(String.valueOf(ticket.getAttachments().size()));
            dto.setTitle(ticket.getTitle());
            dto.setStatus(ticket.getStatus());
            dto.setConversation(String.valueOf(ticket.getMessages().size()));
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
        dto.setTickets(tickets.stream().sorted(Comparator.comparing(TicketDto::getCreatedDate).reversed()).collect(Collectors.toList()));
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
            tickets = ticketRepo.findAll(paging);

            if (ticketRefNo.isPresent()){
                Optional<Ticket> t = ticketRepo.findByTicketRefNo(ticketRefNo.get());
                tickets = addTicketToPage(t,tickets);
            } else if (status.isPresent()) {
                tickets = ticketRepo.findByStatus(paging,getTicketOrderStatusEnum(status.get()));
            } else if (userName.isPresent()) {
                tickets = ticketRepo.findByOpenedBy(paging,userName.get());
            } else if (assignedTo.isPresent()) {
                tickets = ticketRepo.findByAssignedTo(paging,assignedTo.get());
            }else if (userName.isPresent() && status.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndStatus(paging,userName.get(),getTicketOrderStatusEnum(status.get()));
            }else if (assignedTo.isPresent() && status.isPresent()) {
                tickets = ticketRepo.findByAssignedToAndStatus(paging,assignedTo.get(),getTicketOrderStatusEnum(status.get()));
            }else if (userName.isPresent() && assignedTo.isPresent() && status.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndAssignedToAndStatus(paging,userName.get(),assignedTo.get(),getTicketOrderStatusEnum(status.get()));
            }

        } else{
            String loggedUserName = utilService.getAuthUserName();
            tickets = ticketRepo.findByOpenedBy(paging,loggedUserName);
            if (ticketRefNo.isPresent()){
                Optional<Ticket> t = ticketRepo.findByOpenedByAndTicketRefNo(loggedUserName,ticketRefNo.get());
                tickets = addTicketToPage(t,tickets);
            } else if (status.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndStatus(paging,loggedUserName,getTicketOrderStatusEnum(status.get()));
            } else if (assignedTo.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndAssignedTo(paging,loggedUserName,assignedTo.get());
            }else if (assignedTo.isPresent() && status.isPresent()) {
                tickets = ticketRepo.findByOpenedByAndAssignedToAndStatus(paging,loggedUserName,assignedTo.get(),getTicketOrderStatusEnum(status.get()));
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

    public TicketMetricsDto setTicketCounts(List<Ticket> tickets){
        TicketMetricsDto dto = new TicketMetricsDto();
        dto.setOpen(tickets.stream().filter(ticket ->
                ticket.getStatus() == TicketStatus.OPEN ).collect(Collectors.toList()).size());
        dto.setClosed(tickets.stream().filter(ticket ->
                ticket.getStatus() == TicketStatus.CLOSED ).collect(Collectors.toList()).size());
        dto.setTotalTickets(tickets.size());
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

    public String changeTicketStatus(TicketStatusChangeDto dto) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        String userName = utilService.getAuthUserName();
        if (ticket.getOpenedBy().equals(userName) || ticket.getAssignedTo().equals(userName)) {
            if (dto.getStatus().equals("Open")) {
                ticket.setStatus(TicketStatus.OPEN);
                ticket.setOpenedBy(userName);
                ticketRepo.save(ticket);
                return "OK";
            } else if (dto.getStatus().equals("Close")) {
                ticket.setStatus(TicketStatus.CLOSED);
                ticket.setClosedBy(userName);
                ticketRepo.save(ticket);
                return "OK";
            } else {
                throw new NotFoundException("status should be Open or Close");
            }
        }else {
            throw new NotFoundException("user can not change the ticket status");
        }
    }

    public String replyTicket(TicketReplyDto dto) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        String userName = utilService.getAuthUserName();

        if (ticket.getAssignedTo().equals(userName) || ticket.getOpenedBy().equals(userName)) {
            TicketMessage tm = new TicketMessage();
            tm.setMessage(dto.getMessage());
            tm.setAttachments(dto.getAttachments());
            tm.setOwner(userName);
            tm.setTicket(ticket);
            ticketMessageRepo.save(tm);
            return "OK";
        }else {
            throw new NotFoundException("user can not reply to this ticket");
        }
    }

    public List<TicketMessageDto> getThreadTicket(String ticketRefNo,boolean adminUser) {
        Ticket ticket = findByTicketRefNo(ticketRefNo);
        if (adminUser || ticket.getOpenedBy().equals(utilService.getAuthUserName())) {
            List<TicketMessage> tmList = ticketMessageRepo.findAllByTicketTicketRefNoOrderByCreatedDateDesc(ticketRefNo);
            return modelMapper.map(tmList, new TypeToken<List<TicketMessageDto>>() {
            }.getType());
        }else {
            throw new NotFoundException("user can not read the messages");
        }
    }

    public String assignTicket(TicketAssignDto dto) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        ticket.setAssignedTo(dto.getAdminUserId());
        ticketRepo.save(ticket);
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
        List<Ticket> tickets = ticketRepo.findAll();
        if(adminUser){
            return setTicketCounts(tickets);
        } else{
            String loggedUserName = utilService.getAuthUserName();
            List<Ticket> userTickets = tickets.stream().filter(ticket -> ticket.getOpenedBy().equals(loggedUserName)).collect(Collectors.toList());
            return setTicketCounts(userTickets);
        }
    }

    public String updateTicket(String ticketRefNo,TicketUpdateDto dto) {
        Ticket ticket = findByTicketRefNo(ticketRefNo);
        String userName = utilService.getAuthUserName();
        System.out.println(userName);
        System.out.println(ticket.getCreatedBy());
        System.out.println(ticket.getOpenedBy());
        if (ticket.getCreatedBy().equals(userName) || ticket.getOpenedBy().equals(userName)) {
            if (ticket.getStatus().equals(TicketStatus.OPEN)) {
                if (ticket.getMessages().size() == 0) {
                    ticket.setTitle(dto.getTitle());
                    ticket.setDescription(dto.getDescription());
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
                        for (String s : dto.getAttachments()) {
                            attachments.add(s);
                        }
                        ticket.setAttachments(attachments);
                    }
                    ticketRepo.save(ticket);
                    return "OK";
                } else {
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
            TicketMessage latestTicketMessage = null;
            if (ticketMessage.getTicket().getStatus().equals(TicketStatus.OPEN)) {
                if (dto.getTicketRefNo() == null || dto.getTicketRefNo().isEmpty()) {
                    latestTicketMessage = ticketMessageRepo.findFirst1ByTicketTicketRefNoOrderByCreatedDateDesc(ticketMessage.getTicket().getTicketRefNo());
                } else {
                    latestTicketMessage = ticketMessageRepo.findFirst1ByTicketTicketRefNoOrderByCreatedDateDesc(dto.getTicketRefNo());
                }

                if (ticketMessage.getId() == latestTicketMessage.getId()) {
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
}
