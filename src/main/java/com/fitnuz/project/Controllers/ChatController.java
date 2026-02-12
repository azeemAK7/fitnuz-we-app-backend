package com.fitnuz.project.Controllers;

import com.fitnuz.project.Payload.DTO.ChatRequestDto;
import com.fitnuz.project.Payload.DTO.ChatResponseDto;
import com.fitnuz.project.Service.Definations.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/public/chat")
    public ResponseEntity<ChatResponseDto> chat(@RequestBody ChatRequestDto chatRequest) {
        String reply = chatService.getReply(chatRequest.getMessage(), chatRequest.getHistory());
        return new ResponseEntity<>(new ChatResponseDto(reply), HttpStatus.OK);
    }
}
