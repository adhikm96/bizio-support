package com.thebizio.biziosupport.dto.email;

import com.thebizio.biziosupport.enums.NotificationTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HtmlBodyEmailDto {
    private NotificationTypeEnum notificationType;
    private List<String> toEmails = new ArrayList<>();
    private String subject;
    private String htmlBody;
}
