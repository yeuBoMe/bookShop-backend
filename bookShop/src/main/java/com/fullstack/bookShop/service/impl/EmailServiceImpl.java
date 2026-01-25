package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.service.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender javaMailSender;

    private final SpringTemplateEngine springTemplateEngine;

    @Override
    public void sendEmailSync(
            String to,
            String subject,
            String content,
            boolean isMultipart,
            boolean isHtml
    ) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                    mimeMessage,
                    isMultipart,
                    StandardCharsets.UTF_8.name()
            );

            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(content, isHtml);

            javaMailSender.send(mimeMessage);

        } catch (MailException | MessagingException ex) {
            LOGGER.error("ERROR SEND EMAIL >>> {} ", ex.getMessage(), ex);
        }
    }

    @Async
    @Override
    public void sendEmailWithTemplateSync(
            String to,
            String subject,
            String templateName,
            String userName,
            Object value
    ) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("order", value);

        String content = springTemplateEngine.process(templateName, context);
        sendEmailSync(to, subject, content, false, true);
    }
}

