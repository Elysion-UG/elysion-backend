package com.elysion.domain.user;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_token", uniqueConstraints = @UniqueConstraint(columnNames = "token"))
public class UserToken extends PanacheEntityBase {
    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(name = "token", nullable = false, length = 255)
    public String token;

    @Column(name = "type", nullable = false, length = 20)
    public String type; // "ACTIVATION" | "EMAIL_CHANGE"

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "confirmed_at")
    public OffsetDateTime confirmedAt;

    @Column(name = "used_at")
    public OffsetDateTime usedAt;
}