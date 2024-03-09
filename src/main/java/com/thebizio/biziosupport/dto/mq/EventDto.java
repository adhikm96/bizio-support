package com.thebizio.biziosupport.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private String project;
    private String module;
    private String hostName;
    private String eventType;
    private long timestamp;
    private String eType;
    private String actor;
    private String username;
    private String activityGroup;
    private String activity;
    private String activityContent;
    private String payload;
    private boolean log;
    private boolean forward;
    private String event;
    private String org;
    private List<String> notificationIds;

}
