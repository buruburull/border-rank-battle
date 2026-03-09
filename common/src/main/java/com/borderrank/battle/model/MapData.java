package com.borderrank.battle.model;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents a map configuration for arena matches.
 * Stores spawn points, boundaries, and availability state.
 */
public class MapData {

    private final String mapId;
    private final String displayName;
    private final String worldName;
    private final double[][] spawnPoints; // [index][x,y,z]
    private final double[] corner1; // [x,y,z] or null
    private final double[] corner2; // [x,y,z] or null
    private boolean inUse;
    private int currentMatchId;

    public MapData(String mapId, String displayName, String worldName,
                   double[][] spawnPoints, double[] corner1, double[] corner2) {
        this.mapId = mapId;
        this.displayName = displayName;
        this.worldName = worldName;
        this.spawnPoints = spawnPoints;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.inUse = false;
        this.currentMatchId = -1;
    }

    public String getMapId() { return mapId; }
    public String getDisplayName() { return displayName; }
    public String getWorldName() { return worldName; }
    public boolean isInUse() { return inUse; }
    public int getCurrentMatchId() { return currentMatchId; }

    public void setInUse(boolean inUse) { this.inUse = inUse; }
    public void setCurrentMatchId(int matchId) { this.currentMatchId = matchId; }

    /**
     * Get spawn point as a Location for the given index.
     * Returns null if index is out of bounds or world not loaded.
     */
    public Location getSpawnPoint(int index, World world) {
        if (spawnPoints == null || index < 0 || index >= spawnPoints.length) {
            return null;
        }
        double[] point = spawnPoints[index];
        return new Location(world, point[0] + 0.5, point[1], point[2] + 0.5);
    }

    /**
     * Get the number of spawn points defined for this map.
     */
    public int getSpawnPointCount() {
        return spawnPoints != null ? spawnPoints.length : 0;
    }

    /**
     * Check if a location is within this map's boundaries.
     * Returns true if no boundaries are defined (unbounded map).
     */
    public boolean isWithinBoundaries(Location loc) {
        if (corner1 == null || corner2 == null) return true;

        double minX = Math.min(corner1[0], corner2[0]);
        double maxX = Math.max(corner1[0], corner2[0]);
        double minY = Math.min(corner1[1], corner2[1]);
        double maxY = Math.max(corner1[1], corner2[1]);
        double minZ = Math.min(corner1[2], corner2[2]);
        double maxZ = Math.max(corner1[2], corner2[2]);

        return loc.getX() >= minX && loc.getX() <= maxX
            && loc.getY() >= minY && loc.getY() <= maxY
            && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    @Override
    public String toString() {
        return "MapData{id='" + mapId + "', name='" + displayName + "', inUse=" + inUse + "}";
    }
}
