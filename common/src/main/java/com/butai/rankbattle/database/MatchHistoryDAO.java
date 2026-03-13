package com.butai.rankbattle.database;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * DAO for match_history and match_results tables.
 */
public class MatchHistoryDAO {

    private final DatabaseManager db;
    private final Logger logger;

    public MatchHistoryDAO(DatabaseManager db, Logger logger) {
        this.db = db;
        this.logger = logger;
    }

    /**
     * Insert a match_history record and return the generated match_id.
     * Returns -1 on failure.
     */
    public int insertMatchHistory(String matchType, String mapName, Integer seasonId,
                                  String resultType, int durationSec) {
        String sql = "INSERT INTO match_history (match_type, map_name, season_id, result_type, ended_at, duration_sec) "
                + "VALUES (?, ?, ?, ?, NOW(), ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, matchType);
            ps.setString(2, mapName);
            if (seasonId != null && seasonId > 0) {
                ps.setInt(3, seasonId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, resultType);
            ps.setInt(5, durationSec);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to insert match history: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Insert a match_results record for a single player.
     */
    public void insertMatchResult(int dbMatchId, UUID uuid, Integer teamId, String weaponType,
                                  int kills, int deaths, double damageDealt, int etherRemaining,
                                  Double judgeScore, boolean survived, int rpChange, int placement) {
        String sql = "INSERT INTO match_results (match_id, uuid, team_id, weapon_type, kills, deaths, "
                + "damage_dealt, ether_remaining, judge_score, survived, rp_change, placement) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dbMatchId);
            ps.setString(2, uuid.toString());
            if (teamId != null) {
                ps.setInt(3, teamId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, weaponType);
            ps.setInt(5, kills);
            ps.setInt(6, deaths);
            ps.setDouble(7, damageDealt);
            ps.setInt(8, etherRemaining);
            if (judgeScore != null) {
                ps.setDouble(9, judgeScore);
            } else {
                ps.setNull(9, Types.DOUBLE);
            }
            ps.setBoolean(10, survived);
            ps.setInt(11, rpChange);
            ps.setInt(12, placement);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to insert match result: " + e.getMessage());
        }
    }

    /**
     * Get recent match history for a player (most recent first).
     * Returns list of maps with match details.
     */
    public List<Map<String, Object>> getPlayerHistory(UUID uuid, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT mh.match_id, mh.match_type, mh.map_name, mh.result_type, mh.duration_sec, "
                + "mh.started_at, mr.weapon_type, mr.kills, mr.deaths, mr.damage_dealt, "
                + "mr.rp_change, mr.placement, mr.survived "
                + "FROM match_results mr "
                + "JOIN match_history mh ON mr.match_id = mh.match_id "
                + "WHERE mr.uuid = ? "
                + "ORDER BY mh.started_at DESC LIMIT ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("match_id", rs.getInt("match_id"));
                    row.put("match_type", rs.getString("match_type"));
                    row.put("map_name", rs.getString("map_name"));
                    row.put("result_type", rs.getString("result_type"));
                    row.put("duration_sec", rs.getInt("duration_sec"));
                    row.put("started_at", rs.getTimestamp("started_at"));
                    row.put("weapon_type", rs.getString("weapon_type"));
                    row.put("kills", rs.getInt("kills"));
                    row.put("deaths", rs.getInt("deaths"));
                    row.put("damage_dealt", rs.getDouble("damage_dealt"));
                    row.put("rp_change", rs.getInt("rp_change"));
                    row.put("placement", rs.getInt("placement"));
                    row.put("survived", rs.getBoolean("survived"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to get player history: " + e.getMessage());
        }
        return list;
    }
}
