package com.thebizio.biziosupport.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class IncomingEventDto {
    private String key;
    private String payload;
}
