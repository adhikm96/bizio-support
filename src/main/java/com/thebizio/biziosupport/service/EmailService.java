package com.thebizio.biziosupport.service;

import com.thebizio.biziosupport.dto.email.HtmlBodyEmailDto;
import com.thebizio.biziosupport.enums.NotificationTypeEnum;
import com.thebizio.biziosupport.service.rmq.EventDtoService;
import com.thebizio.biziosupport.service.rmq.EventManagerService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Map;

@Service
public class EmailService {

	@Autowired
	EventManagerService eventManagerService;

	@Autowired
	EventDtoService eventDtoService;

	@Value("${hostname}")
	String hostName;

	@Autowired
	private Configuration config;

	@Value("${mq-email-exchange}")
	private String emailExchange;

	@Value("${mq-email-route-key}")
	private String emailRouteKey;

	@Autowired
	RabbitTemplate rabbitTemplate;

	public boolean sendMailOnBizioNotification(HtmlBodyEmailDto emailDto) throws IOException {

		//send email in queue
		rabbitTemplate.convertAndSend(emailExchange, emailRouteKey, emailDto);

		//send email event in queue
		eventManagerService.addEventInQueue(eventDtoService.createEventDto("Bizio-Support", "bizio.email", hostName, "Notification",
				"Info", "System", "sys", "Email", "EmailMsgQueued",
				"Email has been queued", "", true, false));

		return true;
	}

	public boolean sendMailMimeWithHtml(String to, String subject, Map<String, Object> map, String templateName) {
		try {
			Template t = config.getTemplate(templateName);

			String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);

			HtmlBodyEmailDto emailDto = new HtmlBodyEmailDto();

			emailDto.getToEmails().add(to);
			emailDto.setHtmlBody(HtmlUtils.htmlEscape(html));
			emailDto.setNotificationType(NotificationTypeEnum.EMAIL_ONLY);
			emailDto.setSubject(subject);

			return sendMailOnBizioNotification(emailDto);

		} catch (TemplateException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
