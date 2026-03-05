package com.borderrank.battle.model;

import java.time.LocalDateTime;

/**
 * Represents a competitive season.
 */
public class Season {

    private final int id;
    private final String name;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final boolean active;

    public Season(int id, String name, LocalDateTime startDate, LocalDateTime endDate, boolean active) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public boolean isActive() { return active; }
}
