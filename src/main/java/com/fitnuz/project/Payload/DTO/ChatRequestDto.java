package com.fitnuz.project.Payload.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {
    private String message;
    private List<ChatMessageDto> history;
}
