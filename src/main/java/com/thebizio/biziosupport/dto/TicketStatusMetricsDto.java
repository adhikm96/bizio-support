package com.thebizio.biziosupport.dto;

import com.thebizio.biziosupport.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusMetricsDto {

    private TicketStatus status;
    private Long count;

}
