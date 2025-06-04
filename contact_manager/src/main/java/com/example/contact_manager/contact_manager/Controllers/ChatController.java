package com.example.contact_manager.contact_manager.Controllers;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.contact_manager.contact_manager.entities.ChatMessage;

import org.springframework.messaging.handler.annotation.SendTo;

@Controller
public class ChatController {

    //client will send to "/app/sendMessage and it will process it"
    //and then it will send the message to "/topic/messages" which is the topic that the client is subscribed to
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message)
    {
        return message;
    }

    @GetMapping("/user/chat")
    public String showChatPage() {
        return "chat1";
    }

    @PostMapping("/user/chat")
    public String handleChatPost() {
        return showChatPage();
    } 

}
