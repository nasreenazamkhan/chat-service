package com.cg.chatservice.dto;

import com.cg.chatservice.entity.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String sessionUuid;
    private String userId;
    private String title;
    private boolean favorite;
    private int messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SessionResponse from(ChatSession s) {
        return SessionResponse.builder()
                .sessionUuid(s.getSessionUuid())
                .userId(s.getUserId())
                .title(s.getTitle())
                .favorite(s.isFavorite())
                .messageCount(s.getMessageCount())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
