package com.thebizio.biziosupport.controller;

import com.thebizio.biziosupport.dto.*;
import com.thebizio.biziosupport.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/client/tickets")
public class ClientTicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<ResponseMessageDto> createTicket(@RequestBody @Valid TicketCreateDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.createTicket(dto)));
    }

    @GetMapping
    public ResponseEntity<TicketPaginationDto> getAllTicket(@RequestParam Optional<Integer> page, Optional<Integer> size,
                                                            Optional<String> ticketRefNo,Optional<String> status,Optional<String> username,
                                                            Optional<String> assignedTo) {
        return ResponseEntity.ok(ticketService.getAllTicket(page, size,false,ticketRefNo,status,username,assignedTo));
    }

    @PostMapping("/change-status")
    public ResponseEntity<ResponseMessageDto> changeTicketStatus(@RequestBody @Valid TicketStatusChangeDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.changeTicketStatus(dto,false)));
    }

    @PostMapping("/reply")
    public ResponseEntity<ResponseMessageDto> replyTicket(@RequestBody @Valid TicketReplyDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.replyTicket(dto,false)));
    }

    @GetMapping("/thread/{ticketRefNo}")
    public ResponseEntity<RespMsgWithBodyDto> getThreadTicket(@PathVariable(name = "ticketRefNo") String ticketRefNo ) {
        return ResponseEntity.ok(new RespMsgWithBodyDto("OK", ticketService.getThreadTicket(ticketRefNo,false)));
    }

    @GetMapping("/{ticketRefNo}")
    public ResponseEntity<RespMsgWithBodyDto> getTicket(@PathVariable(name = "ticketRefNo") String ticketRefNo ) {
        return ResponseEntity.ok(new RespMsgWithBodyDto("OK", ticketService.getTicket(ticketRefNo,false)));
    }

    @GetMapping("/metrics")
    public ResponseEntity<RespMsgWithBodyDto> getTicketMetrics() {
        return ResponseEntity.ok(new RespMsgWithBodyDto("OK", ticketService.getTicketMetrics(false)));
    }

    @PutMapping("/{ticketRefNo}")
    public ResponseEntity<ResponseMessageDto> updateTicket(@PathVariable(name = "ticketRefNo") String ticketRefNo,
                                                           @RequestBody @Valid TicketUpdateDto dto ) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.updateTicket(ticketRefNo,dto,true)));
    }

    @PutMapping("/reply")
    public ResponseEntity<ResponseMessageDto> updateTicketReply(@RequestBody @Valid TicketUpdateReplyDto dto) {
        return ResponseEntity.ok(new ResponseMessageDto(ticketService.updateTicketReply(dto,false)));
    }
}
