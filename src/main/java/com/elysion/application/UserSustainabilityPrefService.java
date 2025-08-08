package com.elysion.application;

import com.elysion.domain.Importance;
import com.elysion.domain.SustainabilityFilter;
import com.elysion.domain.User;
import com.elysion.domain.UserSustainabilityPref;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserSustainabilityPrefService {

    /**
     * Liefert alle Präferenzen des Users (eine pro Filter).
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<UserSustainabilityPref> getPreferences(User user) {
        if (user == null) throw new IllegalArgumentException("user is null");
        return UserSustainabilityPref.list("user", user);
    }

    /**
     * Liefert die Präferenz für einen konkreten Filter (per filterKey), falls vorhanden.
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<UserSustainabilityPref> getPreference(User user, String filterKey) {
        if (user == null) throw new IllegalArgumentException("user is null");
        if (filterKey == null || filterKey.isBlank()) throw new IllegalArgumentException("filterKey is blank");

        SustainabilityFilter filter = SustainabilityFilter.find("key", filterKey).firstResult();
        if (filter == null) return Optional.empty();

        return Optional.ofNullable(
                UserSustainabilityPref.find("user = ?1 and filter = ?2", user, filter).firstResult()
        );
    }

    /**
     * Setzt oder aktualisiert die Präferenz (Importance) für einen Filter (per filterKey).
     * Gibt die aktuelle/aktualisierte Preference zurück.
     */
    @Transactional
    public UserSustainabilityPref setPreference(User user, String filterKey, Importance importance) {
        if (user == null) throw new IllegalArgumentException("user is null");
        if (filterKey == null || filterKey.isBlank()) throw new IllegalArgumentException("filterKey is blank");
        if (importance == null) throw new IllegalArgumentException("importance is null");

        SustainabilityFilter filter = SustainabilityFilter.find("key", filterKey).firstResult();
        if (filter == null) throw new IllegalArgumentException("Unknown filter key: " + filterKey);

        UserSustainabilityPref pref = UserSustainabilityPref
                .find("user = ?1 and filter = ?2", user, filter)
                .firstResult();

        if (pref == null) {
            pref = new UserSustainabilityPref(UUID.randomUUID(), user, filter, importance);
            pref.persist();
        } else {
            pref.importance = importance;
            // Panache tracked entity -> Flush/merge nicht nötig
        }
        return pref;
    }

    /**
     * Löscht die Präferenz für einen gegebenen Filter (per filterKey), wenn vorhanden.
     * Gibt true zurück, wenn etwas gelöscht wurde.
     */
    @Transactional
    public boolean removePreference(User user, String filterKey) {
        if (user == null) throw new IllegalArgumentException("user is null");
        if (filterKey == null || filterKey.isBlank()) throw new IllegalArgumentException("filterKey is blank");

        SustainabilityFilter filter = SustainabilityFilter.find("key", filterKey).firstResult();
        if (filter == null) return false;

        long deleted = UserSustainabilityPref.delete("user = ?1 and filter = ?2", user, filter);
        return deleted > 0;
    }

    /**
     * Nützlich für das Frontend: Map filterKey -> Importance.
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Map<String, Importance> getPreferenceMap(User user) {
        return getPreferences(user).stream()
                .collect(Collectors.toMap(
                        p -> p.filter.key,
                        p -> p.importance
                ));
    }
}