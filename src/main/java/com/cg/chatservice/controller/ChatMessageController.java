package com.cg.chatservice.controller;


import com.cg.chatservice.config.RestPage;
import com.cg.chatservice.dto.AddMessageRequest;
import com.cg.chatservice.dto.ApiResponse;
import com.cg.chatservice.dto.MessageResponse;
import com.cg.chatservice.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions/{sessionUuid}/messages")
@RequiredArgsConstructor
@Tag(name = "Chat Messages", description = "APIs for adding and retrieving messages within a chat session")
public class ChatMessageController {

    private final ChatMessageService messageService;

    // ── POST /messages ────────────────────────────────────────────────────────

    @Operation(
            summary = "Add a message to a session",
            description = "Adds a new message to an existing chat session. " +
                    "Supported sender types: USER, ASSISTANT, SYSTEM. " +
                    "Optional contextData can store model name, temperature, etc."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Message added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid request — content blank or too long"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Missing X-API-KEY header"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Session belongs to a different user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Session not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> addMessage(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddMessageRequest request) {

        MessageResponse message = messageService.addMessage(sessionUuid, userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message added", message));
    }

    // ── GET /messages (oldest first) ──────────────────────────────────────────

    @Operation(
            summary = "Get message history (oldest first)",
            description = "Returns paginated messages for a session in chronological order " +
                    "(oldest message first). Use this for standard chat history view."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Messages retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Session belongs to a different user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Session not found")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 200)", example = "50")
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        RestPage<MessageResponse> messages =
                messageService.getMessages(sessionUuid, userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(messages));
    }

    // ── GET /messages/latest (newest first) ───────────────────────────────────

    @Operation(
            summary = "Get latest messages (newest first)",
            description = "Returns paginated messages for a session in reverse chronological order " +
                    "(newest message first). Use this for infinite scroll implementations."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Messages retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Session belongs to a different user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Session not found")
    })
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getLatestMessages(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 200)", example = "50")
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        RestPage<MessageResponse> messages =
                messageService.getMessagesDesc(sessionUuid, userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(messages));
    }
}