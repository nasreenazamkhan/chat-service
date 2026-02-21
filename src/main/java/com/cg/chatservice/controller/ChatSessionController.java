package com.cg.chatservice.controller;


import com.cg.chatservice.dto.ApiResponse;
import com.cg.chatservice.dto.CreateSessionRequest;
import com.cg.chatservice.dto.RenameSessionRequest;
import com.cg.chatservice.dto.SessionResponse;
import com.cg.chatservice.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for chat session lifecycle management.
 *
 * <pre>
 *  POST   /api/v1/sessions                            – create session
 *  GET    /api/v1/sessions/{uuid}?userId=             – get session
 *  GET    /api/v1/sessions?userId=&page=&size=        – list sessions
 *  GET    /api/v1/sessions/favorites?userId=          – list favourites
 *  PATCH  /api/v1/sessions/{uuid}/rename?userId=      – rename
 *  PATCH  /api/v1/sessions/{uuid}/favorite?userId=    – toggle favourite
 *  DELETE /api/v1/sessions/{uuid}?userId=             – soft-delete
 * </pre>
 * <p>
 * Note: {@code userId} is passed as a query/header parameter here for
 * simplicity. In production, extract it from a JWT / security context.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class ChatSessionController {


    private final ChatSessionService sessionService;

    // ── POST /sessions ────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest request) {

        SessionResponse session = sessionService.createSession(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Session created successfully", session));
    }

    // ── GET /sessions/{uuid} ──────────────────────────────────────────────────

    @GetMapping("/{sessionUuid}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @PathVariable String sessionUuid,
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                ApiResponse.ok(sessionService.getSession(sessionUuid, userId)));
    }

    // ── GET /sessions ─────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getUserSessions(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<SessionResponse> sessions = sessionService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(sessions));
    }

    // ── GET /sessions/favorites ───────────────────────────────────────────────

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getFavorites(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<SessionResponse> sessions = sessionService.getFavoriteSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(sessions));
    }

    // ── PATCH /sessions/{uuid}/rename ─────────────────────────────────────────

    @PatchMapping("/{sessionUuid}/rename")
    public ResponseEntity<ApiResponse<SessionResponse>> renameSession(
            @PathVariable String sessionUuid,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RenameSessionRequest request) {

        SessionResponse updated = sessionService.renameSession(sessionUuid, userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Session renamed successfully", updated));
    }

    // ── PATCH /sessions/{uuid}/favorite ──────────────────────────────────────

    @PatchMapping("/{sessionUuid}/favorite")
    public ResponseEntity<ApiResponse<SessionResponse>> toggleFavorite(
            @PathVariable String sessionUuid,
            @RequestHeader("X-User-Id") String userId) {

        SessionResponse updated = sessionService.toggleFavorite(sessionUuid, userId);
        return ResponseEntity.ok(ApiResponse.ok("Favourite status toggled", updated));
    }

    // ── DELETE /sessions/{uuid} ───────────────────────────────────────────────

    @DeleteMapping("/{sessionUuid}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable String sessionUuid,
            @RequestHeader("X-User-Id") String userId) {

        sessionService.deleteSession(sessionUuid, userId);
        return ResponseEntity.ok(ApiResponse.ok("Session deleted successfully", null));
    }
}
