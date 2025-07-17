package com.fitnuz.project.Service.Definations;

import jakarta.mail.MessagingException;

public interface MailService {
    void sendOrderReport(String toEmail, String subject, String body, byte[] pdfContent, String fileName) throws MessagingException;
}
