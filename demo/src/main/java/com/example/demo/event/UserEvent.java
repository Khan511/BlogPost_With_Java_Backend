package com.example.demo.event;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import com.example.demo.entities.UserEntity;
import com.example.demo.enumeration.EventType;

@Getter
@Setter
@AllArgsConstructor
public class UserEvent {

    private UserEntity user;
    private EventType eventType;
    private Map<?, ?> data;

}
