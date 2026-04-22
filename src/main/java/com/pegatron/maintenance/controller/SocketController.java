package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.dto.UpdateMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {

    @MessageMapping("/update")
    @SendTo("/topic/updates")
    public UpdateMessage send(UpdateMessage message) {
        return message;
    }
}