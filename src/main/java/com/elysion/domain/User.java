package com.elysion.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "email", nullable = false, unique = true)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "salt", nullable = false)
    public String salt;

    @Column(name = "role", nullable = false)
    public String role;

    @Column(name = "active", nullable = false)
    public boolean active = false;

    @Column(name = "activation_token", unique = true)
    public String activationToken;

    @Column(name = "activation_token_created")
    public OffsetDateTime activationTokenCreated;

    // Convenience constructor
    public User() {
    }

    public User(UUID id, String email, String passwordHash, String salt, String role, OffsetDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.createdAt = createdAt;
    }
}