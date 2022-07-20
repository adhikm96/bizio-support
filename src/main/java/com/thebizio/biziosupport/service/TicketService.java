package com.thebizio.biziosupport.service;

import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.TicketStatus;
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
        return ticketRepo.findByTicketRefNo(ticketRefNo).orElseThrow(() -> new NotFoundException("ticket ref no found"));
    }

    public String createTicket(TicketCreateDto dto) {
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
        ticket.setOpenedBy(utilService.getAuthUserName());
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
        dto.setTickets(tickets);
        dto.setTotalPages(totalPages);
        dto.setPageSize(pageSize);
        return dto;
    }

    public TicketPaginationDto getAllTicket(Optional<Integer> page, Optional<Integer> size, String user,
                                            Optional<String> ticketRefNo,Optional<String> status,Optional<String> userName,
                                            Optional<String> assignedTo) {

        Pageable paging = PageRequest.of(page.orElse(0),size.orElse(10));
        Page<Ticket> tickets = null;

        if(user.equals("admin")){
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

        } else if (user.equals("client")) {
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

        }else{
            return null;
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
        String userEmail = utilService.getAuthUserEmail();

        if (dto.getStatus().equals("Open")){
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setOpenedBy(userEmail);
            ticketRepo.save(ticket);
            return "OK";
        } else if (dto.getStatus().equals("Close")) {
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setClosedBy(userEmail);
            ticketRepo.save(ticket);
            return "OK";
        }else {
            throw new NotFoundException("status should be Open or Close");
        }
    }

    public String replyTicket(TicketReplyDto dto) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        TicketMessage tm = new TicketMessage();
        tm.setMessage(dto.getMessage());
        tm.setAttachments(dto.getAttachments());
        tm.setOwner(utilService.getAuthUserEmail());
        tm.setTicket(ticket);
        ticketMessageRepo.save(tm);
        return "OK";
    }

    public Set<TicketMessage> getThreadTicket(String ticketRefNo) {
        Set<TicketMessage> tms = findByTicketRefNo(ticketRefNo).getMessages();
        return modelMapper.map(tms,new TypeToken<Set<TicketMessageDto>>(){}.getType());
    }

    public String assignTicket(TicketAssignDto dto) {
        Ticket ticket = findByTicketRefNo(dto.getTicketRefNo());
        ticket.setAssignedTo(dto.getAdminUserId());
        ticketRepo.save(ticket);
        return "OK";
    }

    public TicketDetailsDto getTicket(String ticketRefNo) {
        Ticket ticket = findByTicketRefNo(ticketRefNo);
        return modelMapper.map(ticket,TicketDetailsDto.class);
    }

    public Object getTicketMetrics(String user) {
        List<Ticket> tickets = ticketRepo.findAll();
        if(user.equals("admin")){
            return setTicketCounts(tickets);
        } else if (user.equals("client")) {
            String loggedUserName = utilService.getAuthUserName();
            List<Ticket> userTickets = tickets.stream().filter(ticket -> ticket.getOpenedBy().equals(loggedUserName)).collect(Collectors.toList());
            return setTicketCounts(userTickets);
        }else{
            return null;
        }
    }
}
