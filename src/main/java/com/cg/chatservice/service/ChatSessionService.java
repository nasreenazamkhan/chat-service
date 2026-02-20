package com.cg.chatservice.service;

import com.cg.chatservice.dto.CreateSessionRequest;
import com.cg.chatservice.dto.RenameSessionRequest;
import com.cg.chatservice.dto.SessionResponse;
import com.cg.chatservice.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatSessionService {

    SessionResponse createSession(CreateSessionRequest request);

    SessionResponse getSession(String sessionUuid, String userId);

    Page<SessionResponse> getUserSessions(String userId, Pageable pageable);

    Page<SessionResponse> getFavoriteSessions(String userId, Pageable pageable);

    SessionResponse renameSession(String sessionUuid, String userId, RenameSessionRequest request);

    SessionResponse toggleFavorite(String sessionUuid, String userId);

    void deleteSession(String sessionUuid, String userId);

    ChatSession findSessionOwned(String sessionUuid, String userId);

    void enforceSessionLimit(String userId);
}
