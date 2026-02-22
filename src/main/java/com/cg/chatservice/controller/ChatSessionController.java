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
@Tag(name = "Chat Sessions", description = "APIs for managing chat sessions — create, retrieve, rename, favourite and delete")
public class ChatSessionController {

    private final ChatSessionService sessionService;

    // ── POST /sessions ────────────────────────────────────────────────────────

    @Operation(
            summary = "Create a new chat session",
            description = "Creates a new chat session for the specified user. " +
                    "A unique session UUID is generated automatically."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Session created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid request body — userId missing or title too long",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Missing X-API-KEY header",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "Session limit reached for this user",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest request) {

        SessionResponse session = sessionService.createSession(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Session created successfully", session));
    }

    // ── GET /sessions/{uuid} ──────────────────────────────────────────────────

    @Operation(
            summary = "Get a session by UUID",
            description = "Retrieves a single active session. " +
                    "Returns 403 if the session belongs to a different user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Session found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Session belongs to a different user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Session not found")
    })
    @GetMapping("/{sessionUuid}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                ApiResponse.ok(sessionService.getSession(sessionUuid, userId)));
    }

    // ── GET /sessions ─────────────────────────────────────────────────────────

    @Operation(
            summary = "List all sessions for a user",
            description = "Returns a paginated list of all active sessions for the given user, " +
                    "sorted by most recently updated first."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Sessions retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getUserSessions(
            @Parameter(description = "ID of the user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<SessionResponse> sessions = sessionService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(sessions));
    }

    // ── GET /sessions/favorites ───────────────────────────────────────────────

    @Operation(
            summary = "List favourite sessions",
            description = "Returns a paginated list of sessions marked as favourite by the user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Favourite sessions retrieved successfully")
    })
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getFavorites(
            @Parameter(description = "ID of the user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<SessionResponse> sessions = sessionService.getFavoriteSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.paged(sessions));
    }

    // ── PATCH /sessions/{uuid}/rename ─────────────────────────────────────────

    @Operation(
            summary = "Rename a session",
            description = "Updates only the title of an existing session. " +
                    "Other fields remain unchanged."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Session renamed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid title — blank or too long"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Session belongs to a different user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Session not found")
    })
    @PatchMapping("/{sessionUuid}/rename")
    public ResponseEntity<ApiResponse<SessionResponse>> renameSession(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RenameSessionRequest request) {

        SessionResponse updated = sessionService.renameSession(sessionUuid, userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Session renamed successfully", updated));
    }

    // ── PATCH /sessions/{uuid}/favorite ──────────────────────────────────────

    @Operation(
            summary = "Toggle favourite status",
            description = "Toggles the favourite flag of a session. " +
                    "If currently false it becomes true and vice versa. No request body needed."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Favourite status toggled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Session belongs to a different user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Session not found")
    })
    @PatchMapping("/{sessionUuid}/favorite")
    public ResponseEntity<ApiResponse<SessionResponse>> toggleFavorite(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId) {

        SessionResponse updated = sessionService.toggleFavorite(sessionUuid, userId);
        return ResponseEntity.ok(ApiResponse.ok("Favourite status toggled", updated));
    }

    // ── DELETE /sessions/{uuid} ───────────────────────────────────────────────

    @Operation(
            summary = "Delete a session",
            description = "Soft-deletes a session and all its messages. " +
                    "The data is retained in the database but hidden from all queries."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Session deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Session belongs to a different user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Session not found")
    })
    @DeleteMapping("/{sessionUuid}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @Parameter(description = "UUID of the session", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "ID of the requesting user", required = true)
            @RequestHeader("X-User-Id") String userId) {

        sessionService.deleteSession(sessionUuid, userId);
        return ResponseEntity.ok(ApiResponse.ok("Session deleted successfully", null));
    }
}
