package com.cg.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a chat session belonging to a specific user.
 * Soft-delete is implemented via {@code is_deleted} flag.
 */
@Entity
@Table(name = "chat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = 0")        // global soft-delete filter
public class ChatSession implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_uuid", nullable = false, unique = true, length = 36)
    @Builder.Default
    private String sessionUuid = UUID.randomUUID().toString();

    /** External user identifier (e.g. auth token subject / user-id). */
    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "title", nullable = false)
    @Builder.Default
    private String title = "New Chat";

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private boolean favorite = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "message_count", nullable = false)
    @Builder.Default
    private int messageCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ── Relationships ──────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (sessionUuid == null) {
            sessionUuid = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void incrementMessageCount() {
        this.messageCount++;
    }
}