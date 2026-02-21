package com.cg.chatservice.repository;


import com.cg.chatservice.entity.ChatMessage;
import com.cg.chatservice.enums.SenderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Paginated message history for a session (oldest first).
     */
    @Query("""
            SELECT m FROM ChatMessage m
             WHERE m.session.id = :sessionId
               AND m.deleted    = false
             ORDER BY m.createdAt ASC
            """)
    Page<ChatMessage> findActiveBySessionId(@Param("sessionId") Long sessionId, Pageable pageable);

    /**
     * Reverse-paged history (newest first – useful for infinite scroll).
     */
    @Query("""
            SELECT m FROM ChatMessage m
             WHERE m.session.id = :sessionId
               AND m.deleted    = false
             ORDER BY m.createdAt DESC
            """)
    Page<ChatMessage> findActiveBySessionIdDesc(@Param("sessionId") Long sessionId, Pageable pageable);

    Optional<ChatMessage> findByMessageUuid(String messageUuid);

    /**
     * Count active messages in a session.
     */
    @Query("""
            SELECT COUNT(m) FROM ChatMessage m
             WHERE m.session.id = :sessionId
               AND m.deleted    = false
            """)
    long countActiveBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Filter by sender type inside a session.
     */
    @Query("""
            SELECT m FROM ChatMessage m
             WHERE m.session.id  = :sessionId
               AND m.senderType  = :senderType
               AND m.deleted     = false
             ORDER BY m.createdAt ASC
            """)
    Page<ChatMessage> findBySessionIdAndSenderType(
            @Param("sessionId") Long sessionId,
            @Param("senderType") SenderType senderType,
            Pageable pageable);
}
