package se.svent.tasting.invite;

import jakarta.persistence.*;
import se.svent.tasting.common.BaseEntity;
import se.svent.tasting.user.AppUser;

import java.time.Instant;

// one invite link, the real token is never stored here, only its hash
@Entity
@Table(name = "invite")
public class Invite extends BaseEntity {

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by")
    private AppUser usedBy;

    protected Invite() {
    }

    public Invite(String tokenHash, AppUser createdBy, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
    }

    // usable means nobody has used it yet and it has not expired
    public boolean isUsableAt(Instant now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
}
