package com.cg.chatservice.entity;


import com.cg.chatservice.enums.SenderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single message within a {@link ChatSession}.
 * Context data is stored as a flexible JSON blob.
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_uuid", nullable = false, unique = true, length = 36)
    @Builder.Default
    private String messageUuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 16)
    private SenderType senderType;

    /**
     * Nullable: only populated for USER / SYSTEM messages.
     */
    @Column(name = "sender_id", length = 128)
    private String senderId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Arbitrary key-value context (e.g. model name, temperature, references).
     * Stored as JSON in MySQL and transparently mapped.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_data", columnDefinition = "JSON")
    private Map<String, Object> contextData;

    /**
     * Optional token count for cost tracking.
     */
    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (messageUuid == null) {
            messageUuid = UUID.randomUUID().toString();
        }
    }
}