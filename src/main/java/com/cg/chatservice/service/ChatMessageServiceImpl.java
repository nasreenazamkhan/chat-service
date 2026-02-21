package com.cg.chatservice.service;


import com.cg.chatservice.dto.AddMessageRequest;
import com.cg.chatservice.dto.MessageResponse;
import com.cg.chatservice.entity.ChatMessage;
import com.cg.chatservice.entity.ChatSession;
import com.cg.chatservice.repository.ChatMessageRepository;
import com.cg.chatservice.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;

    // ── Add ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.MESSAGES, key = "#sessionUuid + ':' + #userId")
    public MessageResponse addMessage(String sessionUuid, String userId, AddMessageRequest request) {
        ChatSession session = findSessionOwned(sessionUuid, userId);

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .senderType(request.getSenderType())
                .senderId(request.getSenderId())
                .content(request.getContent())
                .contextData(request.getContextData())
                .tokenCount(request.getTokenCount())
                .build();

        message = messageRepository.save(message);
        sessionRepository.incrementMessageCount(session.getId());

        log.debug("Added {} message {} to session {}", request.getSenderType(),
                message.getMessageUuid(), sessionUuid);

        return MessageResponse.from(message);
    }

    // ── Read (oldest first) ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.MESSAGES, key = "#sessionUuid + ':' + #userId + ':asc:' + #pageable.pageNumber")
    public Page<MessageResponse> getMessages(String sessionUuid, String userId, Pageable pageable) {
        ChatSession session = findSessionOwned(sessionUuid, userId);
        return messageRepository
                .findActiveBySessionId(session.getId(), pageable)
                .map(MessageResponse::from);
    }

    // ── Read (newest first) ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.MESSAGES, key = "#sessionUuid + ':' + #userId + ':desc:' + #pageable.pageNumber")
    public Page<MessageResponse> getMessagesDesc(String sessionUuid, String userId, Pageable pageable) {
        ChatSession session = findSessionOwned(sessionUuid, userId);
        return messageRepository
                .findActiveBySessionIdDesc(session.getId(), pageable)
                .map(MessageResponse::from);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ChatSession findSessionOwned(String sessionUuid, String userId) {
        return sessionRepository
                .findBySessionUuidAndUserId(sessionUuid, userId)
                .orElseThrow(() -> {
                    boolean exists = sessionRepository.findBySessionUuid(sessionUuid).isPresent();
                    if (!exists) return ResourceNotFoundException.session(sessionUuid);
                    return AccessDeniedException.session(userId, sessionUuid);
                });
    }
}
