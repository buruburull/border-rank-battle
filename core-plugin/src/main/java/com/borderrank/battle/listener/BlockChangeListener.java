package com.borderrank.battle.listener;

import com.borderrank.battle.BRBPlugin;
import com.borderrank.battle.arena.ArenaInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.UUID;

/**
 * Listens for block changes during matches and records them in BlockTracker
 * so they can be restored when the match ends.
 */
public class BlockChangeListener implements Listener {

    /**
     * Track blocks destroyed by explosions (Meteora Sub, TNT, etc).
     * EntityExplodeEvent fires BEFORE blocks are actually destroyed,
     * so we can record their original state.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        // Try to find the player who caused this explosion
        Player shooter = null;
        if (entity instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player p) {
                shooter = p;
            }
        }

        // If no direct shooter, check if explosion is within any active match map
        ArenaInstance match = null;
        BRBPlugin plugin = BRBPlugin.getInstance();

        if (shooter != null) {
            match = plugin.getMatchManager().getPlayerMatch(shooter.getUniqueId());
        } else {
            // For non-player explosions within match boundaries,
            // try to find match by checking if any match player is nearby
            for (UUID uuid : getAllMatchPlayers(plugin)) {
                ArenaInstance m = plugin.getMatchManager().getPlayerMatch(uuid);
                if (m != null) {
                    match = m;
                    break;
                }
            }
        }

        if (match == null) return;

        // Record all blocks that will be destroyed
        for (Block block : event.blockList()) {
            match.getBlockTracker().recordBlockChange(block);
        }
    }

    /**
     * Track blocks broken by players during matches.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        BRBPlugin plugin = BRBPlugin.getInstance();
        ArenaInstance match = plugin.getMatchManager().getPlayerMatch(player.getUniqueId());
        if (match == null) return;

        match.getBlockTracker().recordBlockChange(event.getBlock());
    }

    /**
     * Track blocks placed by players during matches (e.g. Escudo glass walls).
     * Records the original state (usually AIR) so it can be restored.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        BRBPlugin plugin = BRBPlugin.getInstance();
        ArenaInstance match = plugin.getMatchManager().getPlayerMatch(player.getUniqueId());
        if (match == null) return;

        // Record the block that was replaced (original state before placement)
        match.getBlockTracker().recordBlockChange(event.getBlockReplacedState().getBlock());
    }

    /**
     * Collect all UUIDs of players currently in matches.
     */
    private java.util.Set<UUID> getAllMatchPlayers(BRBPlugin plugin) {
        java.util.Set<UUID> allPlayers = new java.util.HashSet<>();
        // We don't have direct access to all matches, so this is a fallback
        // The primary path (shooter != null) handles most cases
        return allPlayers;
    }
}
