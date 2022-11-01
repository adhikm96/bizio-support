package com.thebizio.biziosupport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thebizio.biziosupport.dto.email.HtmlBodyEmailDto;
import com.thebizio.biziosupport.enums.NotificationTypeEnum;
import com.thebizio.biziosupport.exception.EmailSendFailedException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import javax.validation.constraints.Null;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

	@Value(("${bizio-notification-host-url}"))
	private String BIZIO_NOTIFICATION_HOST_URL;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Configuration config;

	public boolean sendMailOnBizioNotification(HtmlBodyEmailDto dto) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, objectMapper.writeValueAsString(dto));
		Request request = new Request.Builder().url(BIZIO_NOTIFICATION_HOST_URL + "/notifications/user")
				.method("POST", body).addHeader("Content-Type", "application/json").build();
		Response response = client.newCall(request).execute();

		if (response.isSuccessful()) {
			return true;
		}

		throw new EmailSendFailedException(response.body().string());

//		dto.setHtmlBody("&lt;table          align=&quot;center&quot;          border=&quot;0&quot;          cellpadding=&quot;0&quot;          cellspacing=&quot;0&quot;          role=&quot;presentation&quot;          style=&quot;width: 100%&quot;        &gt;          &lt;tbody&gt;            &lt;tr&gt;              &lt;td                style=&quot;                  direction: ltr;                  font-size: 0px;                  padding: 20px 0;                  padding-bottom: 20px;                  padding-top: 0px;                  text-align: center;                &quot;              &gt;                &lt;!--[if mso | IE]&gt;&lt;table role=&quot;presentation&quot; border=&quot;0&quot; cellpadding=&quot;0&quot; cellspacing=&quot;0&quot;&gt;&lt;tr&gt;&lt;td class=&quot;card-outlook&quot; style=&quot;vertical-align:top;width:600px;&quot; &gt;&lt;![endif]--&gt;                &lt;div                  class=&quot;mj-column-per-100 mj-outlook-group-fix card&quot;                  style=&quot;                    font-size: 0px;                    text-align: left;                    direction: ltr;                    display: inline-block;                    vertical-align: top;                    width: 100%;                  &quot;                &gt;                  &lt;table                    border=&quot;0&quot;                    cellpadding=&quot;0&quot;                    cellspacing=&quot;0&quot;                    role=&quot;presentation&quot;                    style=&quot;vertical-align: top&quot;                    width=&quot;100%&quot;                  &gt;                    &lt;tbody&gt;                      &lt;tr&gt;                        &lt;td                          align=&quot;center&quot;                          style=&quot;                            font-size: 0px;                            padding: 10px 25px;                            word-break: break-word;                          &quot;                        &gt;                          &lt;div                            style=&quot;                              font-family: Ubuntu, Helvetica, Arial, sans-serif;                              font-size: 13px;                              font-style: normal;                              font-weight: 400;                              line-height: 38px;                              text-align: center;                              color: #000000;                            &quot;                          &gt;                            &lt;div class=&quot;card__title&quot;&gt;Welcome to TheBizio!&lt;/div&gt;                          &lt;/div&gt;                        &lt;/td&gt;                      &lt;/tr&gt;                      &lt;tr&gt;                        &lt;td                          align=&quot;left&quot;                          style=&quot;                            font-size: 0px;                            padding: 10px 25px;                            word-break: break-word;                          &quot;                        &gt;                          &lt;div                            style=&quot;                              font-family: Ubuntu, Helvetica, Arial, sans-serif;                              font-size: 14px;                              font-style: normal;                              font-weight: 400;                              line-height: 22px;                              text-align: left;                              color: #66737f;                            &quot;                          &gt;                            Hi ,                          &lt;/div&gt;                        &lt;/td&gt;                      &lt;/tr&gt;                      &lt;tr&gt;                        &lt;td                          align=&quot;left&quot;                          style=&quot;                            font-size: 0px;                            padding: 10px 25px;                            word-break: break-word;                          &quot;                        &gt;                          &lt;div                            style=&quot;                              font-family: Ubuntu, Helvetica, Arial, sans-serif;                              font-size: 14px;                              font-style: normal;                              font-weight: 400;                              line-height: 22px;                              text-align: left;                              color: #66737f;                            &quot;                          &gt;                            Thank you for signing up! Please remember your user                            name to login.                          &lt;/div&gt;                        &lt;/td&gt;                      &lt;/tr&gt;                      &lt;tr&gt;                        &lt;td                          align=&quot;left&quot;                          style=&quot;                            font-size: 0px;                            padding: 10px 25px;                            word-break: break-word;                          &quot;                        &gt;                          &lt;div                            style=&quot;                              font-family: Ubuntu, Helvetica, Arial, sans-serif;                              font-size: 14px;                              font-style: normal;                              font-weight: 400;                              line-height: 22px;                              text-align: left;                              color: #66737f;                            &quot;                          &gt;                            User Name:                            &lt;span style=&quot;font-weight: bold&quot;&gt;&lt;/span&gt;                          &lt;/div&gt;                        &lt;/td&gt;                      &lt;/tr&gt;                      &lt;tr&gt;                        &lt;td                          align=&quot;left&quot;                          style=&quot;                            font-size: 0px;                            padding: 10px 25px;                            word-break: break-word;                          &quot;                        &gt;                          &lt;div                            style=&quot;                              font-family: Ubuntu, Helvetica, Arial, sans-serif;                              font-size: 14px;                              font-style: normal;                              font-weight: 400;                              line-height: 22px;                              text-align: left;                              color: #66737f;                            &quot;                          &gt;                            We&rsquo;re excited to get you up and running! Whether                            you&rsquo;re looking to share documents/ pics or speak                            with your team, friends and colleagues, let us show                            you how easy it can be. The Bizio platform is ready                            to get your job done.                          &lt;/div&gt;                        &lt;/td&gt;                      &lt;/tr&gt;                      &lt;tr&gt;                        &lt;td                          align=&quot;center&quot;                          vertical-align=&quot;middle&quot;                          style=&quot;                            font-size: 0px;                            padding: 10px 25px;                            word-break: break-word;                          &quot;                        &gt;                          &lt;table                            border=&quot;0&quot;                            cellpadding=&quot;0&quot;                            cellspacing=&quot;0&quot;                            role=&quot;presentation&quot;                            style=&quot;                              border-collapse: separate;                              width: 245px;                              line-height: 100%;                            &quot;                          &gt;                            &lt;tr&gt;                              &lt;td                                align=&quot;center&quot;                                bgcolor=&quot;#206dff&quot;                                role=&quot;presentation&quot;                                style=&quot;                                  border: none;                                  border-radius: 12px;                                  cursor: auto;                                  mso-padding-alt: 12px 8px 12px 16px;                                  background: #206dff;                                &quot;                                valign=&quot;middle&quot;                              &gt;                                &lt;a                                  href=&quot;#&quot;                                  style=&quot;                                    display: inline-block;                                    width: 221px;                                    background: #206dff;                                    color: #ffffff;                                    font-family: Ubuntu, Helvetica, Arial,                                      sans-serif;                                    font-size: 13px;                                    font-weight: normal;                                    line-height: 120%;                                    margin: 0;                                    text-decoration: none;                                    text-transform: none;                                    padding: 12px 8px 12px 16px;                                    mso-padding-alt: 0px;                                    border-radius: 12px;                                  &quot;                                  target=&quot;_blank&quot;                                &gt;                                  Get Started                                &lt;/a&gt;                              &lt;/td&gt;                            &lt;/tr&gt;                          &lt;/table&gt;                        &lt;/td&gt;                      &lt;/tr&gt;                      &lt;tr&gt;                        &lt;td                          align=&quot;left&quot;                          style=&quot;                            font-size: 0px;                            padding: 10px 25px;                            word-break: break-word;                          &quot;                        &gt;                          &lt;div                            style=&quot;                              font-family: Ubuntu, Helvetica, Arial, sans-serif;                              font-size: 14px;                              font-style: normal;                              font-weight: 400;                              line-height: 22px;                              text-align: left;                              color: #66737f;                            &quot;                          &gt;                            - The Bizio Team                          &lt;/div&gt;                        &lt;/td&gt;                      &lt;/tr&gt;                    &lt;/tbody&gt;                  &lt;/table&gt;                &lt;/div&gt;                &lt;!--[if mso | IE]&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;&lt;![endif]--&gt;              &lt;/td&gt;            &lt;/tr&gt;          &lt;/tbody&gt;        &lt;/table&gt;");
//
//		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//		MultiValueMap<String, Object> reqBody = new LinkedMultiValueMap<>();
//
//		reqBody.set("notificationType", dto.getNotificationType().toString());
//		reqBody.set("toEmails", dto.getToEmails());
//		reqBody.set("subject", dto.getSubject());
//		reqBody.set("htmlBody", dto.getHtmlBody());
//
//		headers.set("Content-Type", "application/json");
//
//		HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(reqBody, headers);
//
//		System.out.println("~~~~~~~~~~~~~~");
//		System.out.println(reqBody);
//		System.out.println(req.getBody());
//
//		ResponseEntity<Object> response = restTemplate.exchange(BIZIO_NOTIFICATION_HOST_URL + "/notifications/user", HttpMethod.GET, req, Object.class, new HashMap<>());
//
//		if(!response.getStatusCode().equals(HttpStatus.OK)){
//			// raise exception
//			throw new EmailSendFailedException(response.getBody());
//		}
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
