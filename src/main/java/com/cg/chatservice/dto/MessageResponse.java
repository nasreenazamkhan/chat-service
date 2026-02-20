package com.cg.chatservice.dto;


import com.cg.chatservice.entity.ChatMessage;
import com.cg.chatservice.enums.SenderType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class MessageResponse {

    private String messageUuid;
    private String sessionUuid;
    private SenderType senderType;
    private String senderId;
    private String content;
    private Map<String, Object> contextData;
    private Integer tokenCount;
    private LocalDateTime createdAt;

    public static MessageResponse from(ChatMessage m) {
        return MessageResponse.builder()
                .messageUuid(m.getMessageUuid())
                .sessionUuid(m.getSession().getSessionUuid())
                .senderType(m.getSenderType())
                .senderId(m.getSenderId())
                .content(m.getContent())
                .contextData(m.getContextData())
                .tokenCount(m.getTokenCount())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
