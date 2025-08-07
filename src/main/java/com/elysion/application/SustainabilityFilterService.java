package com.elysion.application;

import com.elysion.domain.SustainabilityFilter;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SustainabilityFilterService {

    /**
     * Gibt alle Sustainability-Filter zurück.
     * Später hier z.B. Caching, Sortierung oder komplexe Business-Logik ergänzen.
     */
    public List<SustainabilityFilter> getAllFilters() {
        return SustainabilityFilter.listAll();
    }
}