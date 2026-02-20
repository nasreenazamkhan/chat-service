package com.cg.chatservice.service;


import com.cg.chatservice.config.AppProperties;
import com.cg.chatservice.dto.CreateSessionRequest;
import com.cg.chatservice.dto.RenameSessionRequest;
import com.cg.chatservice.dto.SessionResponse;
import com.cg.chatservice.entity.ChatSession;
import com.cg.chatservice.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository sessionRepository;
    private final AppProperties appProperties;

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        enforceSessionLimit(request.getUserId());

        ChatSession session = ChatSession.builder()
                .userId(request.getUserId())
                .title(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "New Chat")
                .build();

        session = sessionRepository.save(session);
        log.info("Created session {} for user {}", session.getSessionUuid(), session.getUserId());
        return SessionResponse.from(session);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.SESSIONS, key = "#sessionUuid + ':' + #userId")
    public SessionResponse getSession(String sessionUuid, String userId) {
        ChatSession session = findSessionOwned(sessionUuid, userId);
        return SessionResponse.from(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getUserSessions(String userId, Pageable pageable) {
        return sessionRepository
                .findByUserIdOrderByUpdatedAtDesc(userId, pageable)
                .map(SessionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getFavoriteSessions(String userId, Pageable pageable) {
        return sessionRepository
                .findByUserIdAndFavoriteTrueOrderByUpdatedAtDesc(userId, pageable)
                .map(SessionResponse::from);
    }

    // ── Update – Rename ───────────────────────────────────────────────────────

    @Override
    @Transactional
    @CachePut(value = CacheNames.SESSIONS, key = "#sessionUuid + ':' + #userId")
    public SessionResponse renameSession(String sessionUuid, String userId, RenameSessionRequest request) {
        ChatSession session = findSessionOwned(sessionUuid, userId);
        session.setTitle(request.getTitle());
        session = sessionRepository.save(session);
        log.info("Renamed session {} to '{}'", sessionUuid, request.getTitle());
        return SessionResponse.from(session);
    }

    // ── Update – Favourite ────────────────────────────────────────────────────

    @Override
    @Transactional
    @CachePut(value = CacheNames.SESSIONS, key = "#sessionUuid + ':' + #userId")
    public SessionResponse toggleFavorite(String sessionUuid, String userId) {
        ChatSession session = findSessionOwned(sessionUuid, userId);
        session.setFavorite(!session.isFavorite());
        session = sessionRepository.save(session);
        log.info("Session {} favorite toggled to {}", sessionUuid, session.isFavorite());
        return SessionResponse.from(session);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.SESSIONS, key = "#sessionUuid + ':' + #userId")
    public void deleteSession(String sessionUuid, String userId) {
        int rows = sessionRepository.softDeleteByUuidAndUserId(sessionUuid, userId);
        if (rows == 0) {
            // Session either didn't exist or doesn't belong to this user
            boolean exists = sessionRepository.findBySessionUuid(sessionUuid).isPresent();
            if (!exists) throw ResourceNotFoundException.session(sessionUuid);
            throw com.chat.service.exception.AccessDeniedException.session(userId, sessionUuid);
        }
        log.info("Soft-deleted session {} by user {}", sessionUuid, userId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ChatSession findSessionOwned(String sessionUuid, String userId) {
        return sessionRepository
                .findBySessionUuidAndUserId(sessionUuid, userId)
                .orElseThrow(() -> {
                    boolean exists = sessionRepository.findBySessionUuid(sessionUuid).isPresent();
                    if (!exists) return ResourceNotFoundException.session(sessionUuid);
                    return com.chat.service.exception.AccessDeniedException.session(userId, sessionUuid);
                });
    }

    private void enforceSessionLimit(String userId) {
        long count = sessionRepository.countByUserId(userId);
        if (count >= appProperties.getMaxSessionsPerUser()) {
            throw new SessionLimitExceededException(appProperties.getMaxSessionsPerUser());
        }
    }
}
