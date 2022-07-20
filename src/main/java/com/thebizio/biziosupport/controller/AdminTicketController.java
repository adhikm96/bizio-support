package com.thebizio.biziosupport.controller;

import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/tickets")
public class AdminTicketController {

    @Autowired
    private TicketService ticketService;


    @PostMapping
    public ResponseEntity<ResponseMessageDto> createTicket(@RequestBody @Valid TicketCreateDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.createTicket(dto)));
    }

    @GetMapping
    public ResponseEntity<TicketPaginationDto> getAllTicket(@RequestParam Optional<Integer> page, Optional<Integer> size,
                                                            Optional<String> ticketRefNo,Optional<String> status,Optional<String> userName,
                                                            Optional<String> assignedTo) {
        return ResponseEntity.ok(ticketService.getAllTicket(page, size, "admin",ticketRefNo,status,userName,assignedTo));
    }

    @PostMapping("change-status")
    public ResponseEntity<ResponseMessageDto> changeTicketStatus(@RequestBody @Valid TicketStatusChangeDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.changeTicketStatus(dto)));
    }

    @PostMapping("reply")
    public ResponseEntity<ResponseMessageDto> replyTicket(@RequestBody @Valid TicketReplyDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.replyTicket(dto)));
    }

    @GetMapping("thread/{ticketRefNo}")
    public ResponseEntity<RespMsgWithBodyDto> getThreadTicket(@PathVariable(name = "ticketRefNo") String ticketRefNo ) {
        return ResponseEntity.ok(new RespMsgWithBodyDto("OK", ticketService.getThreadTicket(ticketRefNo)));
    }

    @PostMapping("assign-ticket")
    public ResponseEntity<ResponseMessageDto> assignTicket(@RequestBody @Valid TicketAssignDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.assignTicket(dto)));
    }

    @GetMapping("/{ticketRefNo}")
    public ResponseEntity<RespMsgWithBodyDto> getTicket(@PathVariable(name = "ticketRefNo") String ticketRefNo ) {
        return ResponseEntity.ok(new RespMsgWithBodyDto("OK", ticketService.getTicket(ticketRefNo)));
    }

    @GetMapping("/metrics")
    public ResponseEntity<RespMsgWithBodyDto> getTicketMetrics() {
        return ResponseEntity.ok(new RespMsgWithBodyDto("OK", ticketService.getTicketMetrics("admin")));
    }
}
