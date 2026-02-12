package com.fitnuz.project.Service.Definations;

import com.fitnuz.project.Payload.DTO.ChatMessageDto;

import java.util.List;

public interface ChatService {
    String getReply(String userMessage, List<ChatMessageDto> history);
}
