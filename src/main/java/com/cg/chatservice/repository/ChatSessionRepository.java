package com.cg.chatservice.repository;


import com.cg.chatservice.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionUuid(String sessionUuid);

    Optional<ChatSession> findBySessionUuidAndUserId(String sessionUuid, String userId);

    /**
     * All active (non-deleted) sessions for a user, newest first.
     */
    Page<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    /**
     * Favorite sessions for a user.
     */
    Page<ChatSession> findByUserIdAndFavoriteTrueOrderByUpdatedAtDesc(String userId, Pageable pageable);

    /**
     * Count active sessions for a user (used to enforce per-user limit).
     */
    long countByUserId(String userId);

    /**
     * Soft-delete via JPQL – bypasses @SQLRestriction on other queries.
     * Sets is_deleted = 1 and records deleted_at timestamp.
     */
    @Modifying
    @Query("""
            UPDATE ChatSession s
               SET s.deleted   = true,
                   s.deletedAt = CURRENT_TIMESTAMP
             WHERE s.sessionUuid = :uuid
               AND s.userId      = :userId
               AND s.deleted     = false
            """)
    int softDeleteByUuidAndUserId(@Param("uuid") String uuid, @Param("userId") String userId);

    /**
     * Increment message counter atomically.
     */
    @Modifying
    @Query("""
            UPDATE ChatSession s
               SET s.messageCount = s.messageCount + 1,
                   s.updatedAt    = CURRENT_TIMESTAMP
             WHERE s.id = :sessionId
            """)
    void incrementMessageCount(@Param("sessionId") Long sessionId);
}
