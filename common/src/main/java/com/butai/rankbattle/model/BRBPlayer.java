package com.butai.rankbattle.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class BRBPlayer {

    private final UUID uuid;
    private String name;
    private RankClass rankClass;
    private int etherCap;
    private final Map<WeaponType, WeaponRP> weaponRPs;

    public BRBPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.rankClass = RankClass.UNRANKED;
        this.etherCap = 1000;
        this.weaponRPs = new EnumMap<>(WeaponType.class);
        for (WeaponType type : WeaponType.values()) {
            weaponRPs.put(type, new WeaponRP(type));
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RankClass getRankClass() {
        return rankClass;
    }

    public void setRankClass(RankClass rankClass) {
        this.rankClass = rankClass;
    }

    public int getEtherCap() {
        return etherCap;
    }

    public void setEtherCap(int etherCap) {
        this.etherCap = etherCap;
    }

    public Map<WeaponType, WeaponRP> getWeaponRPs() {
        return weaponRPs;
    }

    public WeaponRP getWeaponRP(WeaponType type) {
        return weaponRPs.get(type);
    }

    /**
     * Get total RP (sum of all weapon RPs).
     */
    public int getTotalRP() {
        int total = 0;
        for (WeaponRP wrp : weaponRPs.values()) {
            total += wrp.getRp();
        }
        return total;
    }

    /**
     * Get total wins across all weapon types.
     */
    public int getTotalWins() {
        int total = 0;
        for (WeaponRP wrp : weaponRPs.values()) {
            total += wrp.getWins();
        }
        return total;
    }

    /**
     * Get total losses across all weapon types.
     */
    public int getTotalLosses() {
        int total = 0;
        for (WeaponRP wrp : weaponRPs.values()) {
            total += wrp.getLosses();
        }
        return total;
    }

    /**
     * Recalculate rank class based on current total RP.
     * Returns true if rank changed.
     */
    public boolean recalculateRank() {
        RankClass newRank = RankClass.fromTotalRP(getTotalRP());
        if (newRank != this.rankClass) {
            this.rankClass = newRank;
            return true;
        }
        return false;
    }
}
