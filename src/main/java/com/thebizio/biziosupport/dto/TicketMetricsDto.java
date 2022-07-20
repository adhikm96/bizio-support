package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketMetricsDto {
    private Integer open;
    private Integer closed;
    private Integer totalTickets;
}
