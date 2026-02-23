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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions/{sessionUuid}/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Messages", description = "APIs for adding and retrieving messages within a chat session")
public class ChatMessageController {

    private final ChatMessageService messageService;

    @Operation(summary = "Add a message to a session",
            description = "Supported sender types: USER, ASSISTANT, SYSTEM")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Message added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> addMessage(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddMessageRequest request) {
        log.info("Adding {} message to sessionUuid={} userId={}", request.getSenderType(), sessionUuid, userId);
        MessageResponse message = messageService.addMessage(sessionUuid, userId, request);
        log.debug("Message added messageUuid={}", message.getMessageUuid());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message added", message));
    }

    @Operation(summary = "Get message history (oldest first)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "Page size (max 200)", example = "50")
            @RequestParam(defaultValue = "50") int size) {
        log.debug("Getting messages (asc) sessionUuid={} userId={} page={} size={}", sessionUuid, userId, page, size);
        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        RestPage<MessageResponse> messages = messageService.getMessages(sessionUuid, userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(messages));
    }

    @Operation(summary = "Get latest messages (newest first)",
            description = "Returns messages in reverse chronological order — use for infinite scroll")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getLatestMessages(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "Page size (max 200)", example = "50")
            @RequestParam(defaultValue = "50") int size) {
        log.debug("Getting messages (desc) sessionUuid={} userId={} page={} size={}", sessionUuid, userId, page, size);
        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        RestPage<MessageResponse> messages = messageService.getMessagesDesc(sessionUuid, userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(messages));
    }
}