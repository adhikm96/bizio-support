package com.thebizio.biziosupport.service.rmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thebizio.biziosupport.dto.mq.EventDto;
import com.thebizio.biziosupport.enums.events.Actor;
import com.thebizio.biziosupport.enums.events.EType;
import com.thebizio.biziosupport.enums.events.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class EventDtoService {

    final ObjectMapper objectMapper;

    public EventDtoService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EventDto createEventDto(String projectName, String moduleName, String hostName, EventType eventType, EType logType, Actor actor, String username, String activityGroup, String activity, String content, Object payload, boolean log_, boolean forward, String event, String org, List<String> notificationIds) {

        EventDto eventDto = new EventDto();

        eventDto.setProject(projectName);
        eventDto.setModule(moduleName);
        eventDto.setHostName(hostName);
        eventDto.setEventType(eventType.toString());

        // adding current epoch ms
        eventDto.setTimestamp(Instant.now().toEpochMilli());

        eventDto.setEType(logType.toString());
        eventDto.setActor(actor.toString());
        eventDto.setUsername(username);
        eventDto.setActivityGroup(activityGroup);
        eventDto.setActivity(activity);
        eventDto.setActivityContent(content);

        eventDto.setLog(log_);
        eventDto.setForward(forward);

        eventDto.setEvent(event);
        eventDto.setOrg(org);
        eventDto.setNotificationIds(notificationIds);

        if(payload.getClass() == String.class)
            eventDto.setPayload((String) payload);
        else {
            try {
                eventDto.setPayload(objectMapper.writeValueAsString(payload));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        return eventDto;
    }
}
