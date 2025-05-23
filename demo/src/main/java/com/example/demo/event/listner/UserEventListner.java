package com.example.demo.event.listner;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.demo.event.UserEvent;
import com.example.demo.service.EmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserEventListner {
    private final EmailService emailService;

    @EventListener
    public void onUserEvent(UserEvent userEvent) {
        switch (userEvent.getEventType()) {
            case REGISTRATION -> emailService.sendNewAccountEmail(userEvent.getUser().getUsername(),
                    userEvent.getUser().getEmail(), (String) userEvent.getData().get("key"));

            default -> {
            }
        }
    }

}
