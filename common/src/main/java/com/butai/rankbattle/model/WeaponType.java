package com.butai.rankbattle.model;

public enum WeaponType {
    STRIKER,
    GUNNER,
    MARKSMAN;

    /**
     * Get WeaponType from string (case-insensitive).
     * Returns null if not found.
     */
    public static WeaponType fromString(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getDisplayName() {
        return switch (this) {
            case STRIKER -> "ストライカー";
            case GUNNER -> "ガンナー";
            case MARKSMAN -> "マークスマン";
        };
    }

    public String getColor() {
        return switch (this) {
            case STRIKER -> "§c";   // red
            case GUNNER -> "§e";    // yellow
            case MARKSMAN -> "§b";  // aqua
        };
    }
}
