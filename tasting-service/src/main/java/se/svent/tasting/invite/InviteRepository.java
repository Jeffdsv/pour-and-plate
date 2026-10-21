package se.svent.tasting.invite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {

    Optional<Invite> findByTokenHash(String tokenHash);

    // claims an invite in one single step, only if it is unused and not expired
    // returns 1 if this request got it, 0 if it was already used, expired or does not exist
    // two people using the same link at the same moment cannot both get 1
    @Modifying
    @Query(value = """
            UPDATE invite
            SET used_at = :now, used_by = :userId, updated_at = :now
            WHERE token_hash = :tokenHash
              AND used_at IS NULL
              AND expires_at > :now
            """, nativeQuery = true)
    int claim(@Param("tokenHash") String tokenHash,
              @Param("userId") Long userId,
              @Param("now") Instant now);
}
