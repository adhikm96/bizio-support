package com.thebizio.biziosupport.service.rmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thebizio.biziosupport.dto.mq.EventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class EventDtoService {

    final ObjectMapper objectMapper;

    public EventDtoService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EventDto createEventDto(String groupName, String componentName, String hostName, String eventType, String logType, String actor, String username, String activityGroup, String activity, String content, Object payload, boolean log_, boolean forward) {

        EventDto eventDto = new EventDto();

        eventDto.setGroup(groupName);
        eventDto.setComponent(componentName);
        eventDto.setHostName(hostName);
        eventDto.setEventType(eventType);

        // adding current epoch ms
        eventDto.setTimestamp(Instant.now().toEpochMilli());

        eventDto.setEType(logType);
        eventDto.setActor(actor);
        eventDto.setUsername(username);
        eventDto.setActivityGroup(activityGroup);
        eventDto.setActivity(activity);
        eventDto.setActivityContent(content);

        eventDto.setLog(log_);
        eventDto.setForward(forward);

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
