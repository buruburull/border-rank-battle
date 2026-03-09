package com.borderrank.battle.arena;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tracks block changes during a match and restores them when the match ends.
 * Uses Method A: records the ORIGINAL state of each changed block (first change only),
 * then restores all blocks to their original state on match end.
 */
public class BlockTracker {

    private final int matchId;
    // Key: "world:x,y,z" -> original BlockData (before first modification)
    private final Map<String, BlockSnapshot> changedBlocks = new LinkedHashMap<>();

    public BlockTracker(int matchId) {
        this.matchId = matchId;
    }

    /**
     * Record the original state of a block before it is changed.
     * Only records the FIRST change at each position (preserves true original state).
     * Use this for BlockBreakEvent and EntityExplodeEvent (block not yet changed).
     */
    public void recordBlockChange(Block block) {
        String key = blockKey(block);
        if (!changedBlocks.containsKey(key)) {
            changedBlocks.put(key, new BlockSnapshot(block));
        }
    }

    /**
     * Record the original state of a block using explicit BlockData.
     * Use this for BlockPlaceEvent where the block has ALREADY been replaced,
     * so we need the replaced state's BlockData instead of the current block's data.
     */
    public void recordBlockChange(Location location, BlockData originalData) {
        String key = location.getWorld().getName() + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
        if (!changedBlocks.containsKey(key)) {
            changedBlocks.put(key, new BlockSnapshot(location, originalData));
        }
    }

    /**
     * Restore all tracked blocks to their original state.
     * Should be called when the match ends.
     */
    public void restoreAllBlocks() {
        for (Map.Entry<String, BlockSnapshot> entry : changedBlocks.entrySet()) {
            entry.getValue().restore();
        }
        int count = changedBlocks.size();
        changedBlocks.clear();
        if (count > 0) {
            org.bukkit.Bukkit.getLogger().info("[BRB] Match #" + matchId + ": Restored " + count + " blocks.");
        }
    }

    /**
     * Get the number of tracked block changes.
     */
    public int getTrackedCount() {
        return changedBlocks.size();
    }

    /**
     * Check if any blocks have been tracked.
     */
    public boolean isEmpty() {
        return changedBlocks.isEmpty();
    }

    private String blockKey(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + "," + block.getY() + "," + block.getZ();
    }

    /**
     * Snapshot of a block's original state for later restoration.
     */
    private static class BlockSnapshot {
        private final Location location;
        private final BlockData blockData;

        BlockSnapshot(Block block) {
            this.location = block.getLocation();
            this.blockData = block.getBlockData().clone();
        }

        BlockSnapshot(Location location, BlockData blockData) {
            this.location = location.clone();
            this.blockData = blockData.clone();
        }

        void restore() {
            Block block = location.getBlock();
            block.setBlockData(blockData, false); // false = don't apply physics
        }
    }
}
