package com.thebizio.biziosupport.service;

import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.entity.TicketMessage;
import com.thebizio.biziosupport.enums.TicketStatus;
import com.thebizio.biziosupport.exception.NotFoundException;
import com.thebizio.biziosupport.repo.TicketMessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.thebizio.biziosupport.repo.TicketRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {

    @Autowired
    private UtilService utilService;

    @Autowired
    private TicketRepo ticketRepo;

    @Autowired
    private TicketMessageRepo ticketMessageRepo;
    public Ticket findById(UUID id){
        return ticketRepo.findById(id).orElseThrow(() -> new NotFoundException("ticket not found"));
    }

    public Ticket toggleOrder(UUID ticketId, boolean status) {
        Ticket ticket = findById(ticketId);
        ticket.setStatus(status ? TicketStatus.OPEN : TicketStatus.CLOSED);
        return ticketRepo.save(ticket);
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

        ticket.setStatus(TicketStatus.OPEN);
        ticket.setOpenedBy(utilService.getAuthUserEmail());

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

    public TicketPaginationDto getAllTicket(Optional<Integer> page, Optional<Integer> size) {
        Page<Ticket> tickets = ticketRepo.findAll(PageRequest.of(page.orElse(0),size.orElse(10)));
        return mapObjectToPagination(mapTicketEntityToDto(tickets),tickets.getSize(),tickets.getTotalPages());
    }

    public String changeTicketStatus(TicketStatusChangeDto dto) {
        if (dto.getStatus().equals("Open")){
            toggleOrder(dto.getTicketId(),true);
            return "OK";
        } else if (dto.getStatus().equals("Close")) {
            toggleOrder(dto.getTicketId(),false);
            return "OK";
        }else {
            throw new NotFoundException("status should be Open or Close");
        }
    }

    public String replyTicket(TicketReplyDto dto) {
        Ticket ticket = findById(UUID.fromString(dto.getTicketId()));

        TicketMessage tm = new TicketMessage();
        tm.setMessage(dto.getMessage());
        tm.setAttachments(dto.getAttachments());
        tm.setOwner(utilService.getAuthUserEmail());
        tm.setTicket(ticket);
        ticketMessageRepo.save(tm);
        return "OK";
    }
}
