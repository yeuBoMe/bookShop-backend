package com.fullstack.bookShop.service;

public interface EmailService {

    void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml);

    void sendEmailWithTemplateSync(String to, String subject, String templateName, String userName, Object value);
}

