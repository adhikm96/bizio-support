package com.thebizio.biziosupport.controller;

import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;


    @PostMapping
    public ResponseEntity<ResponseMessageDto> createTicket(@RequestBody @Valid TicketCreateDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.createTicket(dto)));
    }

    @GetMapping
    public ResponseEntity<TicketPaginationDto> getAllTicket(@RequestParam Optional<Integer> page, Optional<Integer> size) {
        return ResponseEntity.ok(ticketService.getAllTicket(page, size));
    }

    @PostMapping("change-status")
    public ResponseEntity<ResponseMessageDto> changeTicketStatus(@RequestBody @Valid TicketStatusChangeDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.changeTicketStatus(dto)));
    }

    @PostMapping("reply")
    public ResponseEntity<ResponseMessageDto> replyTicket(@RequestBody @Valid TicketReplyDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.replyTicket(dto)));
    }

}
