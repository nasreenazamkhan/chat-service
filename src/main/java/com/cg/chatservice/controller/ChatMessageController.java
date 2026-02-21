package com.cg.chatservice.controller;

import com.cg.chatservice.dto.AddMessageRequest;
import com.cg.chatservice.dto.ApiResponse;
import com.cg.chatservice.dto.MessageResponse;
import com.cg.chatservice.service.ChatMessageService;
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
 * REST API for chat messages within a session.
 *
 * <pre>
 *  POST  /api/v1/sessions/{uuid}/messages            – add message
 *  GET   /api/v1/sessions/{uuid}/messages            – history (oldest first)
 *  GET   /api/v1/sessions/{uuid}/messages/latest     – history (newest first)
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/sessions/{sessionUuid}/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService messageService;

    // ── POST /messages ────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> addMessage(
            @PathVariable String sessionUuid,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddMessageRequest request) {

        MessageResponse message = messageService.addMessage(sessionUuid, userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message added", message));
    }

    // ── GET /messages (oldest first) ──────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable String sessionUuid,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        Page<MessageResponse> messages = messageService.getMessages(sessionUuid, userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(messages));
    }

    // ── GET /messages/latest (newest first – infinite scroll) ─────────────────

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getLatestMessages(
            @PathVariable String sessionUuid,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        Page<MessageResponse> messages = messageService.getMessagesDesc(sessionUuid, userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(messages));
    }
}