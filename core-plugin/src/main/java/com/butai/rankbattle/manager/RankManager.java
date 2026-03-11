package com.butai.rankbattle.manager;

import com.butai.rankbattle.database.PlayerDAO;
import com.butai.rankbattle.model.BRBPlayer;
import com.butai.rankbattle.model.RankClass;
import com.butai.rankbattle.model.WeaponRP;
import com.butai.rankbattle.model.WeaponType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class RankManager {

    private final PlayerDAO playerDAO;
    private final Logger logger;
    private final Map<UUID, BRBPlayer> playerCache = new ConcurrentHashMap<>();

    public RankManager(PlayerDAO playerDAO, Logger logger) {
        this.playerDAO = playerDAO;
        this.logger = logger;
    }

    /**
     * Load player data from DB into cache. Called on join.
     */
    public BRBPlayer loadPlayer(UUID uuid, String name) {
        // Register or update in DB
        playerDAO.registerOrUpdate(uuid, name);
        playerDAO.initializeWeaponRP(uuid);

        // Build BRBPlayer from DB
        BRBPlayer player = new BRBPlayer(uuid, name);

        // Load rank class
        String rankStr = playerDAO.getRankClass(uuid);
        player.setRankClass(RankClass.fromString(rankStr));

        // Load weapon RPs
        Map<String, Integer> rpMap = playerDAO.getAllWeaponRP(uuid);
        for (WeaponType type : WeaponType.values()) {
            int rp = rpMap.getOrDefault(type.name(), 1000);
            int[] winLoss = playerDAO.getWinLoss(uuid, type.name());
            WeaponRP wrp = new WeaponRP(type, rp, winLoss[0], winLoss[1]);
            player.getWeaponRPs().put(type, wrp);
        }

        // Recalculate rank and sync to DB if changed
        if (player.recalculateRank()) {
            playerDAO.updateRankClass(uuid, player.getRankClass().name());
        }

        playerCache.put(uuid, player);
        logger.info("Loaded player: " + name + " [" + player.getRankClass().getDisplayName() + " / RP:" + player.getTotalRP() + "]");
        return player;
    }

    /**
     * Unload player from cache. Called on quit.
     */
    public void unloadPlayer(UUID uuid) {
        playerCache.remove(uuid);
    }

    /**
     * Get cached player data. Returns null if not loaded.
     */
    public BRBPlayer getPlayer(UUID uuid) {
        return playerCache.get(uuid);
    }

    /**
     * Update RP after a match and recalculate rank.
     */
    public void applyMatchResult(UUID uuid, WeaponType weaponType, int rpChange, boolean isWin) {
        BRBPlayer player = playerCache.get(uuid);
        if (player == null) return;

        WeaponRP wrp = player.getWeaponRP(weaponType);
        wrp.addRp(rpChange);
        if (isWin) {
            wrp.addWin();
        } else {
            wrp.addLoss();
        }

        // Save to DB
        playerDAO.updateMatchResult(uuid, weaponType.name(), rpChange, isWin);

        // Recalculate rank
        if (player.recalculateRank()) {
            playerDAO.updateRankClass(uuid, player.getRankClass().name());
        }
    }

    /**
     * Calculate RP change using asymmetric Elo formula.
     * Returns int[] { winnerGain, loserLoss } (loserLoss is positive value).
     */
    public int[] calculateRP(int winnerRP, int loserRP) {
        int base = 30;
        double coefficient = 1.0 + (double)(loserRP - winnerRP) / 1000.0;
        int winnerGain = (int) Math.round(base * coefficient);
        winnerGain = Math.max(5, Math.min(120, winnerGain));

        int loserLoss = (int) Math.round(winnerGain * 0.7);
        loserLoss = Math.max(5, loserLoss);

        // Add participation bonus
        int participationBonus = 5;
        winnerGain += participationBonus;
        loserLoss -= participationBonus; // net loss is reduced by bonus
        if (loserLoss < 0) loserLoss = 0;

        return new int[]{winnerGain, loserLoss};
    }

    /**
     * Get top players (delegates to DAO).
     */
    public List<Map<String, Object>> getTopPlayers(int limit) {
        return playerDAO.getTopPlayers(limit);
    }

    /**
     * Get top players by weapon type.
     */
    public List<Map<String, Object>> getTopByWeapon(String weaponType, int limit) {
        return playerDAO.getTopByWeapon(weaponType, limit);
    }

    /**
     * Get all cached players count.
     */
    public int getCachedPlayerCount() {
        return playerCache.size();
    }
}
