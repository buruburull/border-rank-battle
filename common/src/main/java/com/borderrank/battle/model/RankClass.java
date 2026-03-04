package com.borderrank.battle.model;

import net.md_5.bungee.api.ChatColor;

/**
 * Represents player rank classes with display names and colors.
 */
public enum RankClass {
    UNRANKED("未所属", ChatColor.WHITE),
    C("C級", ChatColor.GRAY),
    B("B級", ChatColor.BLUE),
    A("A級", ChatColor.GOLD),
    S("S級", ChatColor.LIGHT_PURPLE);

    private final String displayName;
    private final ChatColor color;

    RankClass(String displayName, ChatColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }
}
