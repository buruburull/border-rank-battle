package com.butai.rankbattle.model;

public enum RankClass {
    S(15000, "§6§l", "S級"),
    A(10000, "§c", "A級"),
    B(5000, "§b", "B級"),
    C(0, "§a", "C級"),
    UNRANKED(0, "§7", "未所属");

    private final int requiredRP;
    private final String color;
    private final String displayName;

    RankClass(int requiredRP, String color, String displayName) {
        this.requiredRP = requiredRP;
        this.color = color;
        this.displayName = displayName;
    }

    public int getRequiredRP() {
        return requiredRP;
    }

    public String getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredName() {
        return color + displayName;
    }

    /**
     * Determine rank class from total RP (sum of all weapon RPs).
     */
    public static RankClass fromTotalRP(int totalRP) {
        if (totalRP >= S.requiredRP) return S;
        if (totalRP >= A.requiredRP) return A;
        if (totalRP >= B.requiredRP) return B;
        return C;
    }

    public static RankClass fromString(String name) {
        if (name == null) return UNRANKED;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNRANKED;
        }
    }
}
