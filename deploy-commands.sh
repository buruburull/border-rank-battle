#!/bin/bash
# BRB Plugin Update - 2026-03-04
# Run this on GCP server to apply all changes

echo "=== Updating RankCommand.java ==="
cat > ~/border-rank-battle/core-plugin/src/main/java/com/borderrank/battle/command/RankCommand.java << 'JAVAEOF'
package com.borderrank.battle.command;

import com.borderrank.battle.BRBPlugin;
import com.borderrank.battle.manager.RankManager;
import com.borderrank.battle.model.BRBPlayer;
import com.borderrank.battle.model.WeaponRP;
import com.borderrank.battle.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RankCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.sendInfoMessage(player, "Usage: /rank <solo|cancel|stats|top>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "solo" -> handleSolo(player);
            case "cancel" -> handleCancel(player);
            case "stats" -> handleStats(player, args);
            case "top" -> handleTop(player, args);
            default -> MessageUtil.sendErrorMessage(player, "Unknown subcommand: " + subcommand);
        }

        return true;
    }

    private void handleSolo(Player player) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        if (!plugin.getQueueManager().isInQueue(player.getUniqueId())) {
            plugin.getQueueManager().addToSoloQueue(player.getUniqueId());
            MessageUtil.sendSuccessMessage(player, "ソロキューに参加しました！対戦相手を待っています...");
        } else {
            MessageUtil.sendErrorMessage(player, "既にキューに参加しています！");
        }
    }

    private void handleCancel(Player player) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        if (plugin.getQueueManager().removePlayer(player.getUniqueId())) {
            MessageUtil.sendSuccessMessage(player, "キューから退出しました。");
        } else {
            MessageUtil.sendErrorMessage(player, "キューに参加していません！");
        }
    }

    private void handleStats(Player player, String[] args) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        Player targetPlayer = player;

        if (args.length > 1) {
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                MessageUtil.sendErrorMessage(player, "Player not found: " + args[1]);
                return;
            }
        }

        RankManager rankManager = plugin.getRankManager();
        BRBPlayer brPlayer = rankManager.getPlayer(targetPlayer.getUniqueId());

        if (brPlayer == null) {
            MessageUtil.sendErrorMessage(player, "No data found for player: " + targetPlayer.getName());
            return;
        }

        MessageUtil.sendInfoMessage(player, "=== Ranking Stats for " + targetPlayer.getName() + " ===");
        MessageUtil.sendInfoMessage(player, "Overall Rank: " + rankManager.getHighestRankTier(brPlayer));

        var weaponRPs = brPlayer.getWeaponRPs();
        for (var entry : weaponRPs.entrySet()) {
            var wrp = entry.getValue();
            MessageUtil.sendInfoMessage(player,
                entry.getKey().name() + " RP: " + wrp.getRp() +
                " (" + wrp.getWins() + "勝 " + wrp.getLosses() + "敗)");
        }
    }

    private void handleTop(Player player, String[] args) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        RankManager rankManager = plugin.getRankManager();

        List<BRBPlayer> topPlayers = rankManager.getGlobalTopPlayers(10);

        if (topPlayers.isEmpty()) {
            MessageUtil.sendInfoMessage(player, "No ranking data available.");
            return;
        }

        MessageUtil.sendInfoMessage(player, "=== Top 10 Rankings (Overall) ===");
        int rank = 1;
        for (BRBPlayer brPlayer : topPlayers) {
            int rp = brPlayer.getTotalRP();
            MessageUtil.sendInfoMessage(player, "#" + rank + " " + brPlayer.getPlayerName() + " - " + rp + " RP");
            rank++;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("solo");
            completions.add("cancel");
            completions.add("stats");
            completions.add("top");
        } else if (args.length == 2) {
            if ("stats".equalsIgnoreCase(args[0])) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}
JAVAEOF

echo "=== Updating TriggerCommand.java ==="
cat > ~/border-rank-battle/core-plugin/src/main/java/com/borderrank/battle/command/TriggerCommand.java << 'JAVAEOF'
package com.borderrank.battle.command;

import com.borderrank.battle.BRBPlugin;
import com.borderrank.battle.manager.LoadoutManager;
import com.borderrank.battle.manager.TriggerRegistry;
import com.borderrank.battle.model.Loadout;
import com.borderrank.battle.model.TriggerData;
import com.borderrank.battle.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TriggerCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.sendInfoMessage(player, "Usage: /trigger <list|set|remove|view|preset>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "list" -> handleList(player, args);
            case "set" -> handleSet(player, args);
            case "remove" -> handleRemove(player, args);
            case "view" -> handleView(player, args);
            case "preset" -> handlePreset(player, args);
            default -> MessageUtil.sendErrorMessage(player, "Unknown subcommand: " + subcommand);
        }

        return true;
    }

    private void handleList(Player player, String[] args) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        TriggerRegistry registry = plugin.getTriggerRegistry();
        String category = args.length > 1 ? args[1].toUpperCase() : null;

        MessageUtil.sendInfoMessage(player, "=== Available Triggers ===");

        Map<String, TriggerData> triggers = registry.getAll();
        for (TriggerData trigger : triggers.values()) {
            if (category == null || trigger.getCategory().name().equalsIgnoreCase(category)) {
                MessageUtil.sendInfoMessage(player,
                    trigger.getId() + " - " + trigger.getName() +
                    " | Cost: " + trigger.getCost() +
                    " | Trion: " + trigger.getTrionUse());
            }
        }
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendErrorMessage(player, "Usage: /trigger set <slot 1-8> <trigger_id>");
            return;
        }

        BRBPlugin plugin = BRBPlugin.getInstance();
        TriggerRegistry registry = plugin.getTriggerRegistry();
        LoadoutManager loadoutManager = plugin.getLoadoutManager();

        int slot;
        try {
            slot = Integer.parseInt(args[1]);
            if (slot < 1 || slot > 8) {
                MessageUtil.sendErrorMessage(player, "Slot must be between 1 and 8.");
                return;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendErrorMessage(player, "Invalid slot number.");
            return;
        }

        String triggerId = args[2].toLowerCase();
        TriggerData trigger = registry.get(triggerId);

        if (trigger == null) {
            MessageUtil.sendErrorMessage(player, "Trigger not found: " + triggerId);
            return;
        }

        UUID uuid = player.getUniqueId();
        int slotIndex = slot - 1;

        Loadout loadout = loadoutManager.getLoadout(uuid, "default");
        if (loadout == null) {
            loadout = new Loadout(uuid, "default");
            try {
                loadoutManager.saveLoadout(loadout);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create default loadout: " + e.getMessage());
            }
        }

        loadout.setSlot(slotIndex, triggerId);
        loadout.calculateTotalCost(registry.getAll());

        if (loadout.getTotalCost() > 15) {
            loadout.setSlot(slotIndex, "");
            loadout.calculateTotalCost(registry.getAll());
            MessageUtil.sendErrorMessage(player, "TP超過！ コスト: " + (loadout.getTotalCost() + trigger.getCost()) + "/15");
            return;
        }

        try {
            loadoutManager.saveLoadout(loadout);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save loadout: " + e.getMessage());
        }

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(trigger.getMcItem());
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.bukkit.ChatColor.GREEN + trigger.getName());
            List<String> lore = new ArrayList<>();
            lore.add(org.bukkit.ChatColor.GRAY + trigger.getDescription());
            lore.add(org.bukkit.ChatColor.YELLOW + "Cost: " + trigger.getCost() + " TP");
            if (trigger.getTrionUse() > 0) {
                lore.add(org.bukkit.ChatColor.AQUA + "Trion: " + trigger.getTrionUse());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        player.getInventory().setItem(slotIndex, item);

        MessageUtil.sendSuccessMessage(player, trigger.getName() + " をスロット " + slot + " にセット (Cost: " + loadout.getTotalCost() + "/15 TP)");
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendErrorMessage(player, "Usage: /trigger remove <slot 1-8>");
            return;
        }

        BRBPlugin plugin = BRBPlugin.getInstance();
        LoadoutManager loadoutManager = plugin.getLoadoutManager();
        TriggerRegistry registry = plugin.getTriggerRegistry();

        int slot;
        try {
            slot = Integer.parseInt(args[1]);
            if (slot < 1 || slot > 8) {
                MessageUtil.sendErrorMessage(player, "Slot must be between 1 and 8.");
                return;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendErrorMessage(player, "Invalid slot number.");
            return;
        }

        UUID uuid = player.getUniqueId();
        int slotIndex = slot - 1;

        Loadout loadout = loadoutManager.getLoadout(uuid, "default");
        if (loadout != null) {
            loadout.setSlot(slotIndex, "");
            loadout.calculateTotalCost(registry.getAll());
            try {
                loadoutManager.saveLoadout(loadout);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save loadout: " + e.getMessage());
            }
        }

        player.getInventory().setItem(slotIndex, null);
        MessageUtil.sendSuccessMessage(player, "スロット " + slot + " からトリガーを解除しました");
    }

    private void handleView(Player player, String[] args) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        LoadoutManager loadoutManager = plugin.getLoadoutManager();
        TriggerRegistry registry = plugin.getTriggerRegistry();

        Loadout loadout = loadoutManager.getLoadout(player.getUniqueId(), "default");

        MessageUtil.sendInfoMessage(player, "=== Your Loadout ===");

        if (loadout == null || !loadout.isActive()) {
            MessageUtil.sendInfoMessage(player, "No triggers equipped. Use /trigger set <slot> <id>");
            return;
        }

        for (int i = 0; i < 8; i++) {
            String triggerId = loadout.getSlot(i);
            if (triggerId != null && !triggerId.isEmpty()) {
                TriggerData td = registry.get(triggerId);
                String name = td != null ? td.getName() : triggerId;
                int cost = td != null ? td.getCost() : 0;
                String slotLabel = (i < 4) ? "Main" : "Sub";
                MessageUtil.sendInfoMessage(player, "  Slot " + (i + 1) + " [" + slotLabel + "]: " + name + " (Cost: " + cost + ")");
            }
        }

        loadout.calculateTotalCost(registry.getAll());
        MessageUtil.sendInfoMessage(player, "Total Cost: " + loadout.getTotalCost() + "/15 TP");
    }

    private void handlePreset(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendErrorMessage(player, "Usage: /trigger preset <save|load> <name>");
            return;
        }

        String action = args[1].toLowerCase();
        String presetName = args.length > 2 ? args[2] : "";

        if ("save".equalsIgnoreCase(action)) {
            MessageUtil.sendSuccessMessage(player, "Preset '" + presetName + "' saved!");
        } else if ("load".equalsIgnoreCase(action)) {
            MessageUtil.sendSuccessMessage(player, "Preset '" + presetName + "' loaded!");
        } else {
            MessageUtil.sendErrorMessage(player, "Unknown preset action: " + action);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("list");
            completions.add("set");
            completions.add("remove");
            completions.add("view");
            completions.add("preset");
        } else if (args.length == 2) {
            if ("set".equalsIgnoreCase(args[0])) {
                for (int i = 1; i <= 8; i++) {
                    completions.add(String.valueOf(i));
                }
            } else if ("remove".equalsIgnoreCase(args[0])) {
                for (int i = 1; i <= 8; i++) {
                    completions.add(String.valueOf(i));
                }
            } else if ("preset".equalsIgnoreCase(args[0])) {
                completions.add("save");
                completions.add("load");
            }
        } else if (args.length == 3 && "set".equalsIgnoreCase(args[0])) {
            BRBPlugin plugin = BRBPlugin.getInstance();
            TriggerRegistry registry = plugin.getTriggerRegistry();
            completions.addAll(registry.getAll().keySet());
        }

        return completions;
    }
}
JAVAEOF

echo "=== Updating ArenaInstance.java ==="
cat > ~/border-rank-battle/core-plugin/src/main/java/com/borderrank/battle/arena/ArenaInstance.java << 'JAVAEOF'
package com.borderrank.battle.arena;

import com.borderrank.battle.BRBPlugin;
import com.borderrank.battle.manager.LoadoutManager;
import com.borderrank.battle.manager.RankManager;
import com.borderrank.battle.manager.ScoreboardManager;
import com.borderrank.battle.manager.TriggerRegistry;
import com.borderrank.battle.manager.TrionManager;
import com.borderrank.battle.model.Loadout;
import com.borderrank.battle.model.TriggerData;
import com.borderrank.battle.model.WeaponType;
import com.borderrank.battle.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ArenaInstance {

    public enum ArenaState {
        WAITING, COUNTDOWN, ACTIVE, ENDING, FINISHED
    }

    private final int matchId;
    private ArenaState state;
    private final Set<UUID> players;
    private final Set<UUID> alivePlayers;
    private final Map<UUID, Integer> kills;
    private final Map<UUID, WeaponType> playerWeaponTypes;
    private final String mapName;
    private final long startTime;
    private final int timeLimitSec;
    private int countdownRemaining;
    private long lastTickTime;

    public ArenaInstance(int matchId, String mapName, int timeLimitSec) {
        this.matchId = matchId;
        this.mapName = mapName;
        this.timeLimitSec = timeLimitSec;
        this.state = ArenaState.WAITING;
        this.players = new HashSet<>();
        this.alivePlayers = new HashSet<>();
        this.kills = new HashMap<>();
        this.playerWeaponTypes = new HashMap<>();
        this.startTime = System.currentTimeMillis();
        this.countdownRemaining = 10;
        this.lastTickTime = System.currentTimeMillis();
    }

    public void start() {
        BRBPlugin plugin = BRBPlugin.getInstance();
        LoadoutManager loadoutManager = plugin.getLoadoutManager();
        TriggerRegistry triggerRegistry = plugin.getTriggerRegistry();
        TrionManager trionManager = plugin.getTrionManager();

        state = ArenaState.COUNTDOWN;
        alivePlayers.addAll(players);

        for (UUID uuid : players) {
            kills.put(uuid, 0);
        }

        int spawnIndex = 0;
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            Location spawnLoc = new Location(player.getWorld(), spawnIndex * 10, 64, 0);
            player.teleport(spawnLoc);
            player.getInventory().clear();

            Loadout loadout = loadoutManager.getLoadout(uuid, "default");
            if (loadout != null) {
                List<String> slots = loadout.getSlots();
                for (int i = 0; i < Math.min(slots.size(), 8); i++) {
                    String triggerId = slots.get(i);
                    if (triggerId != null && !triggerId.isEmpty()) {
                        TriggerData td = triggerRegistry.get(triggerId);
                        if (td != null) {
                            ItemStack item = new ItemStack(td.getMcItem());
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName(ChatColor.GREEN + td.getName());
                                List<String> lore = new ArrayList<>();
                                lore.add(ChatColor.GRAY + td.getDescription());
                                meta.setLore(lore);
                                item.setItemMeta(meta);
                            }
                            player.getInventory().setItem(i, item);
                        }
                    }
                }

                WeaponType wt = loadoutManager.getWeaponType(loadout, triggerRegistry);
                if (wt != null) {
                    playerWeaponTypes.put(uuid, wt);
                } else {
                    playerWeaponTypes.put(uuid, WeaponType.ATTACKER);
                }
            } else {
                playerWeaponTypes.put(uuid, WeaponType.ATTACKER);
            }

            trionManager.initPlayer(uuid, 1000);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);

            MessageUtil.sendMessage(player, ChatColor.YELLOW + "マッチ開始！カウントダウン: " + countdownRemaining);
            spawnIndex++;
        }
    }

    public void onKill(UUID killer, UUID victim) {
        if (killer != null && kills.containsKey(killer)) {
            kills.put(killer, kills.get(killer) + 1);

            Player killerPlayer = Bukkit.getPlayer(killer);
            Player victimPlayer = Bukkit.getPlayer(victim);
            if (killerPlayer != null && victimPlayer != null) {
                MessageUtil.sendSuccessMessage(killerPlayer, victimPlayer.getName() + " を撃破！ (計" + kills.get(killer) + "キル)");
            }
        }

        alivePlayers.remove(victim);

        Player victimPlayer = Bukkit.getPlayer(victim);
        if (victimPlayer != null) {
            MessageUtil.sendErrorMessage(victimPlayer, "ベイルアウト！");
        }

        if (alivePlayers.size() <= 1) {
            end();
        }
    }

    public void tick() {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastTickTime;
        lastTickTime = currentTime;

        switch (state) {
            case COUNTDOWN -> tickCountdown();
            case ACTIVE -> tickActive(deltaMs);
            case ENDING -> tickEnding();
        }
    }

    private void tickCountdown() {
        countdownRemaining--;

        if (countdownRemaining <= 0) {
            state = ArenaState.ACTIVE;
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    MessageUtil.sendSuccessMessage(player, ChatColor.BOLD + "バトル開始！");
                }
            }
        } else if (countdownRemaining <= 5) {
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    MessageUtil.sendMessage(player, ChatColor.YELLOW + "" + countdownRemaining + "...");
                }
            }
        }
    }

    private void tickActive(long deltaMs) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        ScoreboardManager scoreboardManager = plugin.getScoreboardManager();

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                scoreboardManager.updatePlayerScore(player, kills.getOrDefault(uuid, 0));
            }
        }

        long elapsedSec = (System.currentTimeMillis() - startTime) / 1000;

        long remaining = timeLimitSec - elapsedSec;
        if (remaining == 60 || remaining == 30 || remaining == 10) {
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    MessageUtil.sendMessage(player, ChatColor.RED + "残り" + remaining + "秒！");
                }
            }
        }

        if (elapsedSec >= timeLimitSec) {
            end();
        }
    }

    private void tickEnding() {
        state = ArenaState.FINISHED;
    }

    public void end() {
        state = ArenaState.ENDING;

        BRBPlugin plugin = BRBPlugin.getInstance();
        RankManager rankManager = plugin.getRankManager();

        List<Map.Entry<UUID, Integer>> sortedPlayers = new ArrayList<>(kills.entrySet());
        sortedPlayers.sort((a, b) -> {
            boolean aAlive = alivePlayers.contains(a.getKey());
            boolean bAlive = alivePlayers.contains(b.getKey());
            if (aAlive != bAlive) return bAlive ? 1 : -1;
            return Integer.compare(b.getValue(), a.getValue());
        });

        int placement = 1;
        for (Map.Entry<UUID, Integer> entry : sortedPlayers) {
            UUID uuid = entry.getKey();
            int playerKills = entry.getValue();
            boolean survived = alivePlayers.contains(uuid);

            int rpGain = rankManager.calculateTeamRP(placement, playerKills, survived);

            WeaponType wt = playerWeaponTypes.getOrDefault(uuid, WeaponType.ATTACKER);
            rankManager.addPlayerRP(uuid, wt.name(), rpGain);

            var brPlayer = rankManager.getPlayer(uuid);
            if (brPlayer != null) {
                var wrp = brPlayer.getWeaponRP(wt);
                if (wrp != null) {
                    if (placement == 1) {
                        wrp.setWins(wrp.getWins() + 1);
                    } else {
                        wrp.setLosses(wrp.getLosses() + 1);
                    }
                }
                rankManager.savePlayer(brPlayer);
            }

            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                String rpText = rpGain >= 0 ? (ChatColor.GREEN + "+" + rpGain) : (ChatColor.RED + "" + rpGain);
                MessageUtil.sendInfoMessage(player, "=== マッチ結果 ===");
                MessageUtil.sendInfoMessage(player, "順位: #" + placement + " | キル: " + playerKills + " | RP: " + rpText);

                player.getInventory().clear();
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
            }

            placement++;
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                MessageUtil.sendInfoMessage(player, "--- ランキング ---");
                int rank = 1;
                for (Map.Entry<UUID, Integer> entry : sortedPlayers) {
                    Player p = Bukkit.getPlayer(entry.getKey());
                    String name = p != null ? p.getName() : "Unknown";
                    String marker = alivePlayers.contains(entry.getKey()) ? " ★" : "";
                    MessageUtil.sendInfoMessage(player, "#" + rank + " " + name + " - " + entry.getValue() + "キル" + marker);
                    rank++;
                }
            }
        }
    }

    public Set<UUID> getAlivePlayers() { return new HashSet<>(alivePlayers); }
    public void addPlayer(UUID uuid) { players.add(uuid); }
    public int getMatchId() { return matchId; }
    public ArenaState getState() { return state; }
    public Set<UUID> getPlayers() { return new HashSet<>(players); }
    public int getPlayerKills(UUID uuid) { return kills.getOrDefault(uuid, 0); }
    public String getMapName() { return mapName; }
}
JAVAEOF

echo "=== Updating BRBPlugin.java ==="
cat > ~/border-rank-battle/core-plugin/src/main/java/com/borderrank/battle/BRBPlugin.java << 'JAVAEOF'
package com.borderrank.battle;

import com.borderrank.battle.database.DatabaseManager;
import com.borderrank.battle.database.LoadoutDAO;
import com.borderrank.battle.database.PlayerDAO;
import com.borderrank.battle.listener.CombatListener;
import com.borderrank.battle.listener.PlayerConnectionListener;
import com.borderrank.battle.listener.TriggerUseListener;
import com.borderrank.battle.manager.LoadoutManager;
import com.borderrank.battle.manager.MapManager;
import com.borderrank.battle.manager.QueueManager;
import com.borderrank.battle.manager.RankManager;
import com.borderrank.battle.manager.ScoreboardManager;
import com.borderrank.battle.manager.TrionManager;
import com.borderrank.battle.manager.TriggerRegistry;
import com.borderrank.battle.command.RankCommand;
import com.borderrank.battle.command.TriggerCommand;
import com.borderrank.battle.command.TeamCommand;
import com.borderrank.battle.command.AdminCommand;
import com.borderrank.battle.arena.MatchManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BRBPlugin extends JavaPlugin {

    private static BRBPlugin instance;

    private DatabaseManager databaseManager;
    private TriggerRegistry triggerRegistry;
    private LoadoutManager loadoutManager;
    private TrionManager trionManager;
    private RankManager rankManager;
    private QueueManager queueManager;
    private MapManager mapManager;
    private ScoreboardManager scoreboardManager;
    private MatchManager matchManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        String mysqlHost = getConfig().getString("database.host", "localhost");
        int mysqlPort = getConfig().getInt("database.port", 3306);
        String mysqlDatabase = getConfig().getString("database.name", "brb_game");
        String mysqlUsername = getConfig().getString("database.user", "root");
        String mysqlPassword = getConfig().getString("database.password", "");

        databaseManager = new DatabaseManager(mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername, mysqlPassword);
        try {
            databaseManager.init();
            getLogger().info("Database connected successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }

        triggerRegistry = new TriggerRegistry(this);
        loadoutManager = new LoadoutManager(new LoadoutDAO(databaseManager));
        trionManager = new TrionManager();
        rankManager = new RankManager(new PlayerDAO(databaseManager));
        queueManager = new QueueManager();
        mapManager = new MapManager(this);
        scoreboardManager = new ScoreboardManager();
        matchManager = new MatchManager();

        getCommand("rank").setExecutor(new RankCommand());
        getCommand("trigger").setExecutor(new TriggerCommand());
        getCommand("team").setExecutor(new TeamCommand());
        getCommand("bradmin").setExecutor(new AdminCommand());

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new TriggerUseListener(), this);

        startTickingTasks();

        getLogger().info("Border Rank Battle plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Border Rank Battle plugin disabled!");
    }

    private void startTickingTasks() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (matchManager != null) {
                matchManager.tick();
            }
        }, 0, 20);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (queueManager == null || matchManager == null) return;

            java.util.Set<java.util.UUID> matched = queueManager.trySoloMatch(2);
            if (!matched.isEmpty()) {
                int matchId = matchManager.createSoloMatch(matched, "arena_default");
                if (matchId > 0) {
                    getLogger().info("Solo match #" + matchId + " created with " + matched.size() + " players");
                    for (java.util.UUID uuid : matched) {
                        org.bukkit.entity.Player player = getServer().getPlayer(uuid);
                        if (player != null) {
                            com.borderrank.battle.util.MessageUtil.sendSuccessMessage(player, "マッチが見つかりました！マッチ #" + matchId);
                        }
                    }
                }
            }
        }, 100, 100);
    }

    public static BRBPlugin getInstance() { return instance; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public TriggerRegistry getTriggerRegistry() { return triggerRegistry; }
    public LoadoutManager getLoadoutManager() { return loadoutManager; }
    public TrionManager getTrionManager() { return trionManager; }
    public RankManager getRankManager() { return rankManager; }
    public QueueManager getQueueManager() { return queueManager; }
    public MapManager getMapManager() { return mapManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public MatchManager getMatchManager() { return matchManager; }
}
JAVAEOF

echo "=== Building ==="
cd ~/border-rank-battle
./gradle-8.5/bin/gradle :core-plugin:shadowJar

echo "=== Deploying ==="
cp core-plugin/build/libs/BorderRankBattle-0.1.0-SNAPSHOT.jar ~/minecraft-server/plugins/BorderRankBattle.jar

echo "=== Done! Restart the server to apply changes ==="
