package com.elysion.domain.user;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_sustainability_pref",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","filter_id"}))
public class UserSustainabilityPref extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filter_id", nullable = false)
    public SustainabilityFilter filter;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 20)
    public Importance importance;

    public UserSustainabilityPref() {}

    public UserSustainabilityPref(UUID id, User user,
                                  SustainabilityFilter filter,
                                  Importance importance) {
        this.id = id;
        this.user = user;
        this.filter = filter;
        this.importance = importance;
    }
}