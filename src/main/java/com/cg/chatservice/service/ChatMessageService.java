package com.cg.chatservice.service;

import com.cg.chatservice.config.RestPage;
import com.cg.chatservice.dto.AddMessageRequest;
import com.cg.chatservice.dto.MessageResponse;
import org.springframework.data.domain.Pageable;

public interface ChatMessageService {
    MessageResponse addMessage(String sessionUuid, String userId, AddMessageRequest request);

    /**
     * Paginated message history for a session (oldest first).
     */
    RestPage<MessageResponse> getMessages(String sessionUuid, String userId, Pageable pageable);

    /**
     * Reverse-paged messages (newest first – for infinite scroll).
     */
    RestPage<MessageResponse> getMessagesDesc(String sessionUuid, String userId, Pageable pageable);
}
