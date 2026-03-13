package com.butai.rankbattle.listener;

import com.butai.rankbattle.manager.RankManager;
import com.butai.rankbattle.model.BRBPlayer;
import com.butai.rankbattle.model.RankClass;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles chat prefix ([S級] PlayerName) and tab list formatting.
 */
public class ChatTabListener implements Listener {

    private final RankManager rankManager;

    public ChatTabListener(RankManager rankManager) {
        this.rankManager = rankManager;
    }

    /**
     * Set tab list name on join.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Delay 1 tick to ensure player data is loaded
        player.getServer().getScheduler().runTaskLater(
                player.getServer().getPluginManager().getPlugin("BUTAIRankBattle"),
                () -> updateTabName(player),
                20L
        );
    }

    /**
     * Add rank prefix to chat messages.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        BRBPlayer data = rankManager.getPlayer(player.getUniqueId());
        if (data == null) return;

        RankClass rank = data.getRankClass();
        String prefix = rank.getColor() + "[" + rank.getDisplayName() + "] §r";

        // Set the chat renderer to include the rank prefix
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component prefixComp = LegacyComponentSerializer.legacySection().deserialize(prefix);
            Component separator = LegacyComponentSerializer.legacySection().deserialize(" §8» §f");
            return prefixComp
                    .append(sourceDisplayName)
                    .append(separator)
                    .append(message);
        });
    }

    /**
     * Update a player's tab list name with rank prefix.
     */
    public void updateTabName(Player player) {
        BRBPlayer data = rankManager.getPlayer(player.getUniqueId());
        if (data == null) return;

        RankClass rank = data.getRankClass();
        String tabName = rank.getColor() + "[" + rank.getDisplayName() + "] §f" + player.getName();
        player.playerListName(LegacyComponentSerializer.legacySection().deserialize(tabName));
    }
}
