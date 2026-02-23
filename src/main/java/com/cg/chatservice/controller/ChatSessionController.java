package com.cg.chatservice.controller;

import com.cg.chatservice.dto.ApiResponse;
import com.cg.chatservice.dto.CreateSessionRequest;
import com.cg.chatservice.dto.RenameSessionRequest;
import com.cg.chatservice.dto.SessionResponse;
import com.cg.chatservice.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Sessions", description = "APIs for managing chat sessions")
public class ChatSessionController {

    private final ChatSessionService sessionService;

    @Operation(summary = "Create a new chat session")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Session created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing API key",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest request) {
        log.info("Creating session for userId={}", request.getUserId());
        SessionResponse session = sessionService.createSession(request);
        log.info("Session created sessionUuid={} userId={}", session.getSessionUuid(), request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Session created successfully", session));
    }

    @Operation(summary = "Get a session by UUID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Session found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{sessionUuid}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId) {
        log.debug("Fetching session sessionUuid={} userId={}", sessionUuid, userId);
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getSession(sessionUuid, userId)));
    }

    @Operation(summary = "List all sessions for a user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getUserSessions(
            @Parameter(description = "ID of the user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Listing sessions userId={} page={} size={}", userId, page, size);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<SessionResponse> sessions = sessionService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(sessions));
    }

    @Operation(summary = "List favourite sessions")
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getFavorites(
            @Parameter(description = "ID of the user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Listing favourites userId={}", userId);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<SessionResponse> sessions = sessionService.getFavoriteSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(sessions));
    }

    @Operation(summary = "Rename a session")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Renamed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @PatchMapping("/{sessionUuid}/rename")
    public ResponseEntity<ApiResponse<SessionResponse>> renameSession(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RenameSessionRequest request) {
        log.info("Renaming session sessionUuid={} userId={} newTitle={}", sessionUuid, userId, request.getTitle());
        SessionResponse updated = sessionService.renameSession(sessionUuid, userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Session renamed successfully", updated));
    }

    @Operation(summary = "Toggle favourite status")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Toggled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @PatchMapping("/{sessionUuid}/favorite")
    public ResponseEntity<ApiResponse<SessionResponse>> toggleFavorite(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId) {
        log.info("Toggling favourite sessionUuid={} userId={}", sessionUuid, userId);
        SessionResponse updated = sessionService.toggleFavorite(sessionUuid, userId);
        return ResponseEntity.ok(ApiResponse.ok("Favourite status toggled", updated));
    }

    @Operation(summary = "Delete a session (soft-delete)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{sessionUuid}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId) {
        log.info("Deleting session sessionUuid={} userId={}", sessionUuid, userId);
        sessionService.deleteSession(sessionUuid, userId);
        return ResponseEntity.ok(ApiResponse.ok("Session deleted successfully", null));
    }
}