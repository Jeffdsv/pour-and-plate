package se.svent.tasting.common;


import jakarta.persistence.*;

import java.time.Instant;

// Shared fields for ALL entities.
@MappedSuperclass
public abstract class BaseEntity {

    // Unique id for the row. Postgres creates it. Never reused, can have gaps.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}