package com.thebizio.biziosupport.service;

import com.thebizio.biziosupport.dto.TicketPaginationDto;
import com.thebizio.biziosupport.dto.TicketCreateDto;
import com.thebizio.biziosupport.dto.TicketDto;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.enums.TicketStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.thebizio.biziosupport.repo.TicketRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private UtilService utilService;

    @Autowired
    private TicketRepo ticketRepo;


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
}
