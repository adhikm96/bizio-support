package com.thebizio.biziosupport.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSendFailedException extends RuntimeException {

    Logger logger = LoggerFactory.getLogger(EmailSendFailedException.class);

    public EmailSendFailedException(Object body) {
        super(body.toString());
        logger.error("failed to send email");
        logger.error(body.toString());
    }
}
