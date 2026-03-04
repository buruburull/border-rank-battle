package com.borderrank.battle.database;

import com.borderrank.battle.model.Loadout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data access object for loadout database operations.
 */
public class LoadoutDAO {
    private final DatabaseManager dbManager;

    /**
     * Constructs a LoadoutDAO instance.
     *
     * @param dbManager the database manager
     */
    public LoadoutDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Loads all loadouts for a player from the database.
     *
     * @param playerId the player's UUID
     * @return a list of loadouts
     */
    public List<Loadout> loadPlayerLoadouts(UUID playerId) {
        List<Loadout> loadouts = new ArrayList<>();
        String sql = "SELECT loadout_name, slots FROM player_loadouts WHERE player_uuid = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("loadout_name");
                    String slotsStr = rs.getString("slots");
                    String[] slots = slotsStr != null ? slotsStr.split(",", -1) : new String[8];
                    Loadout loadout = new Loadout(playerId, name, slots);
                    loadouts.add(loadout);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return loadouts;
    }

    /**
     * Saves a loadout to the database.
     *
     * @param loadout the loadout to save
     */
    public void saveLoadout(Loadout loadout) {
        String sql = "INSERT INTO player_loadouts (player_uuid, loadout_name, slots) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE slots = VALUES(slots)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loadout.getPlayerUuid().toString());
            stmt.setString(2, loadout.getName());
            stmt.setString(3, String.join(",", loadout.getSlots()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a loadout from the database.
     *
     * @param playerId the player's UUID
     * @param name the loadout name
     */
    public void deleteLoadout(UUID playerId, String name) {
        String sql = "DELETE FROM player_loadouts WHERE player_uuid = ? AND loadout_name = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
