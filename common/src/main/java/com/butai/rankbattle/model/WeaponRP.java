package com.butai.rankbattle.model;

public class WeaponRP {

    private final WeaponType weaponType;
    private int rp;
    private int wins;
    private int losses;

    public WeaponRP(WeaponType weaponType) {
        this(weaponType, 1000, 0, 0);
    }

    public WeaponRP(WeaponType weaponType, int rp, int wins, int losses) {
        this.weaponType = weaponType;
        this.rp = rp;
        this.wins = wins;
        this.losses = losses;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public int getRp() {
        return rp;
    }

    public void setRp(int rp) {
        this.rp = rp;
    }

    public void addRp(int amount) {
        this.rp += amount;
        if (this.rp < 0) this.rp = 0;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public void addWin() {
        this.wins++;
    }

    public void addLoss() {
        this.losses++;
    }

    public int getTotalMatches() {
        return wins + losses;
    }

    public double getWinRate() {
        int total = getTotalMatches();
        if (total == 0) return 0.0;
        return (double) wins / total * 100.0;
    }
}
