package com.example.demo.service.serviceImplemnetaion;

import static com.example.demo.constant.Constant.NEW_USER_ACCOUNT_VERIFICAITON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.example.demo.service.EmailService;
import static com.example.demo.utils.EmailUtils.getEmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService {

    private final JavaMailSender sender;

    @Value("${spring.mail.verify.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendNewAccountEmail(String name, String email, String token) {
        System.out.println("Eamil is being sent: " + email);
        try {
            var message = new SimpleMailMessage();

            message.setSubject(NEW_USER_ACCOUNT_VERIFICAITON);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(getEmailMessage(name, host, token));
            sender.send(message);

        } catch (Exception e) {
            log.error("Faile to send email to {}. Error: {}", email, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }

    }

}
