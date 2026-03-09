package com.borderrank.battle.manager;

import com.borderrank.battle.model.MapData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

/**
 * Manages available maps and arena selection for matches.
 * Tracks map availability (available/in_use) and assigns maps to matches.
 */
public class MapManager {

    private final Map<String, MapData> mapRegistry = new LinkedHashMap<>();
    private final Random random = new Random();
    private final JavaPlugin plugin;

    public MapManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads map definitions from a FileConfiguration (triggers.yml).
     * Called by BRBPlugin after TriggerRegistry has resolved the triggers.yml file.
     */
    public void loadMaps(File triggersFile) {
        mapRegistry.clear();

        if (triggersFile == null || !triggersFile.exists()) {
            plugin.getLogger().warning("[MapManager] triggers.yml file not provided or not found, no maps loaded.");
            return;
        }

        plugin.getLogger().info("[MapManager] Loading maps from: " + triggersFile.getAbsolutePath());
        FileConfiguration config = YamlConfiguration.loadConfiguration(triggersFile);
        ConfigurationSection mapsSection = config.getConfigurationSection("maps");
        if (mapsSection == null) {
            plugin.getLogger().info("No maps section in triggers.yml, no maps loaded.");
            return;
        }

        for (String mapId : mapsSection.getKeys(false)) {
            ConfigurationSection mapSection = mapsSection.getConfigurationSection(mapId);
            if (mapSection == null) continue;

            String displayName = mapSection.getString("display_name", mapId);
            String worldName = mapSection.getString("world", "world");

            // Parse spawn points ("x,y,z" format)
            List<String> spawnStrings = mapSection.getStringList("spawn_points");
            if (spawnStrings.isEmpty()) {
                plugin.getLogger().warning("Map '" + mapId + "' has no spawn_points, skipping.");
                continue;
            }

            double[][] spawnPoints = new double[spawnStrings.size()][3];
            boolean valid = true;
            for (int i = 0; i < spawnStrings.size(); i++) {
                String[] parts = spawnStrings.get(i).split(",");
                if (parts.length < 3) {
                    plugin.getLogger().warning("Map '" + mapId + "' spawn point #" + i + " invalid: " + spawnStrings.get(i));
                    valid = false;
                    break;
                }
                try {
                    spawnPoints[i][0] = Double.parseDouble(parts[0].trim());
                    spawnPoints[i][1] = Double.parseDouble(parts[1].trim());
                    spawnPoints[i][2] = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Map '" + mapId + "' spawn point #" + i + " parse error: " + spawnStrings.get(i));
                    valid = false;
                    break;
                }
            }
            if (!valid) continue;

            // Parse boundaries (optional)
            double[] corner1 = null;
            double[] corner2 = null;
            ConfigurationSection boundSection = mapSection.getConfigurationSection("boundaries");
            if (boundSection != null) {
                corner1 = parseCoord(boundSection.getString("corner1"));
                corner2 = parseCoord(boundSection.getString("corner2"));
            }

            MapData mapData = new MapData(mapId, displayName, worldName, spawnPoints, corner1, corner2);
            mapRegistry.put(mapId, mapData);
            plugin.getLogger().info("Map loaded: " + displayName + " (" + mapId + ") with " + spawnPoints.length + " spawn points");
        }

        plugin.getLogger().info("Total maps loaded: " + mapRegistry.size());
    }

    /**
     * Parse "x,y,z" coordinate string to double array.
     */
    private double[] parseCoord(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        if (parts.length < 3) return null;
        try {
            return new double[]{
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim())
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Select a random available (not in use) map. Marks it as in_use.
     * Returns null if no maps are available.
     */
    public MapData selectRandomMap() {
        List<MapData> available = new ArrayList<>();
        for (MapData map : mapRegistry.values()) {
            if (!map.isInUse()) {
                available.add(map);
            }
        }
        if (available.isEmpty()) return null;

        MapData selected = available.get(random.nextInt(available.size()));
        selected.setInUse(true);
        return selected;
    }

    /**
     * Release a map back to the available pool.
     */
    public void releaseMap(String mapId) {
        MapData map = mapRegistry.get(mapId);
        if (map != null) {
            map.setInUse(false);
            map.setCurrentMatchId(-1);
            plugin.getLogger().info("Map released: " + map.getDisplayName() + " (" + mapId + ")");
        }
    }

    /**
     * Get a specific map by ID.
     */
    public MapData getMapData(String mapId) {
        return mapRegistry.get(mapId);
    }

    /**
     * Get all registered maps.
     */
    public List<MapData> getAllMaps() {
        return new ArrayList<>(mapRegistry.values());
    }

    /**
     * Get only available (not in use) maps.
     */
    public List<MapData> getAvailableMaps() {
        List<MapData> available = new ArrayList<>();
        for (MapData map : mapRegistry.values()) {
            if (!map.isInUse()) available.add(map);
        }
        return available;
    }

    /**
     * Get total number of maps.
     */
    public int getMapCount() {
        return mapRegistry.size();
    }

    /**
     * Get number of available maps.
     */
    public int getAvailableCount() {
        int count = 0;
        for (MapData map : mapRegistry.values()) {
            if (!map.isInUse()) count++;
        }
        return count;
    }

    /**
     * Check if a map exists.
     */
    public boolean mapExists(String mapId) {
        return mapRegistry.containsKey(mapId);
    }

    /**
     * Reset all map states to available (recovery after server crash).
     */
    public void resetMapStates() {
        for (MapData map : mapRegistry.values()) {
            map.setInUse(false);
            map.setCurrentMatchId(-1);
        }
        plugin.getLogger().info("All map states reset to available.");
    }

    /**
     * Reload maps from config. Resolves triggers.yml the same way TriggerRegistry does.
     */
    public void reloadMaps() {
        File triggersFile = new File(plugin.getDataFolder(), "triggers.yml");
        if (!triggersFile.exists()) {
            File configFolder = new File(plugin.getServer().getWorldContainer(), "config");
            triggersFile = new File(configFolder, "triggers.yml");
        }
        loadMaps(triggersFile);
    }
}
