package com.thebizio.biziosupport.dto;

import com.thebizio.biziosupport.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
public class TicketCreateDto {

    private TicketType ticketType;

    @NotNull(message = "must not be null or blank")
    @NotBlank(message = "must not be null or blank")
    private String title;
    private String description;
    private DeviceType deviceType;
    private OsEnum os;
    private ApplicationEnum application;
    private BrowserEnum browser;
    private Set<String> attachments = new HashSet<>();
    private String osVersion;
    private String applicationVersion;
    private String browserVersion;
}
