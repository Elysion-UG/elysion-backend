package com.elysion.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "sustainability_filter")
public class SustainabilityFilter extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    /** Eindeutiger Schlüssel (z.B. "bio", "ethical-work"…) */
    @Column(name = "filter_key", nullable = false, unique = true, length = 50)
    public String key;

    /** Lesbarer Name */
    @Column(name = "label", nullable = false, length = 100)
    public String label;

    /** Icon-Name oder -Pfad (im Frontend gemappt auf dein Icon-Enum) */
    @Column(name = "icon", nullable = false, length = 100)
    public String icon;

    /** Ausführliche Beschreibung */
    @Column(name = "description", nullable = false, length = 500)
    public String description;

    /** Beispiele (kommagetrennt oder JSON-Array) */
    @Column(name = "examples", nullable = false, length = 1000)
    public String examples;

    /** Wie wichtig ist dir dieses Kriterium? */
    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 20)
    public Importance importance;

    public SustainabilityFilter() {
        // JPA benötigt
    }

    public SustainabilityFilter(UUID id, String key, String label, String icon,
                                String description, String examples,
                                Importance importance) {
        this.id = id;
        this.key = key;
        this.label = label;
        this.icon = icon;
        this.description = description;
        this.examples = examples;
        this.importance = importance;
    }
}