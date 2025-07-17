package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Service.Definations.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    // Constructor-based injection is recommended for required dependencies
    @Autowired
    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOrderReport(String toEmail, String subject, String body, byte[] pdfContent, String fileName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, false); // false = plain text, set true for HTML

        // Attach PDF
        helper.addAttachment(fileName, new ByteArrayResource(pdfContent));

        mailSender.send(message);
    }
}
