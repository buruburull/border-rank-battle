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
 * Schema: player_loadouts(loadout_id, uuid, loadout_name, slot_1..slot_8, total_cost, created_at, last_modified)
 */
public class LoadoutDAO {
    private final DatabaseManager dbManager;

    public LoadoutDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Loads all loadouts for a player from the database.
     */
    public List<Loadout> loadPlayerLoadouts(UUID playerId) {
        List<Loadout> loadouts = new ArrayList<>();
        String sql = "SELECT loadout_name, slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8 " +
                     "FROM player_loadouts WHERE uuid = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("loadout_name");
                    String[] slots = new String[8];
                    for (int i = 0; i < 8; i++) {
                        String val = rs.getString("slot" + (i + 1));
                        slots[i] = val != null ? val : "";
                    }
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
     * Saves a loadout to the database (INSERT or UPDATE on duplicate).
     */
    public void saveLoadout(Loadout loadout) {
        String sql = "INSERT INTO player_loadouts (uuid, loadout_name, slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "slot1 = VALUES(slot1), slot2 = VALUES(slot2), slot3 = VALUES(slot3), slot4 = VALUES(slot4), " +
                     "slot5 = VALUES(slot5), slot6 = VALUES(slot6), slot7 = VALUES(slot7), slot8 = VALUES(slot8)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loadout.getPlayerUuid().toString());
            stmt.setString(2, loadout.getName());

            List<String> slots = loadout.getSlots();
            for (int i = 0; i < 8; i++) {
                String val = (i < slots.size()) ? slots.get(i) : "";
                // Store empty strings as NULL for foreign key compatibility
                stmt.setString(3 + i, (val != null && !val.isEmpty()) ? val : null);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a loadout from the database.
     */
    public void deleteLoadout(UUID playerId, String name) {
        String sql = "DELETE FROM player_loadouts WHERE uuid = ? AND loadout_name = ?";

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
