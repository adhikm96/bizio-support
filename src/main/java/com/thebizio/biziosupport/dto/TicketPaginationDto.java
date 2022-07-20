package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TicketPaginationDto {

    private List<TicketDto> tickets;
    private Integer totalPages;
    private Integer pageSize;
}
