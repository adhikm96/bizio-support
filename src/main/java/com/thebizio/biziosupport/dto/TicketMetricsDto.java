package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Data
public class TicketMetricsDto {

    private Long open;
    private Long closed;
    private Long totalTickets;

    public TicketMetricsDto(){
        this.open = Long.valueOf(0);
        this.closed = Long.valueOf(0);
        this.totalTickets = Long.valueOf(0);
    }
}
