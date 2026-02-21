package com.cg.chatservice.service;

import com.cg.chatservice.dto.AddMessageRequest;
import com.cg.chatservice.dto.MessageResponse;
import com.cg.chatservice.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatMessageService {
    MessageResponse addMessage(String sessionUuid, String userId, AddMessageRequest request);
    Page<MessageResponse> getMessages(String sessionUuid, String userId, Pageable pageable);
    Page<MessageResponse> getMessagesDesc(String sessionUuid, String userId, Pageable pageable);
    ChatSession findSessionOwned(String sessionUuid, String userId);
}
