package com.butai.rankbattle.command;

import com.butai.rankbattle.manager.FrameRegistry;
import com.butai.rankbattle.manager.QueueManager;
import com.butai.rankbattle.manager.RankManager;
import com.butai.rankbattle.model.BRBPlayer;
import com.butai.rankbattle.model.WeaponType;
import com.butai.rankbattle.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles /bradmin commands: frame reload, forcestart, rp set
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final RankManager rankManager;
    private final QueueManager queueManager;
    private final FrameRegistry frameRegistry;

    public AdminCommand(RankManager rankManager, QueueManager queueManager, FrameRegistry frameRegistry) {
        this.rankManager = rankManager;
        this.queueManager = queueManager;
        this.frameRegistry = frameRegistry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("brb.admin")) {
            sender.sendMessage("§c権限がありません。");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "frame" -> handleFrame(sender, args);
            case "forcestart" -> handleForceStart(sender);
            case "rp" -> handleRP(sender, args);
            default -> {
                sendUsage(sender);
                yield true;
            }
        };
    }

    private boolean handleFrame(CommandSender sender, String[] args) {
        if (args.length < 2 || !"reload".equalsIgnoreCase(args[1])) {
            sender.sendMessage("§e使用法: /bradmin frame reload");
            return true;
        }

        File dataFolder = Bukkit.getPluginManager().getPlugin("BUTAIRankBattle").getDataFolder();
        File framesFile = new File(dataFolder, "frames.yml");
        frameRegistry.loadFromFile(framesFile);
        sender.sendMessage("§aframes.yml を再読み込みしました。(登録フレーム数: " + frameRegistry.getAllFrames().size() + ")");
        return true;
    }

    private boolean handleForceStart(CommandSender sender) {
        // Force start a match from the queue
        sender.sendMessage("§e強制開始機能は未実装です。");
        return true;
    }

    private boolean handleRP(CommandSender sender, String[] args) {
        // /bradmin rp set <player> <weapon> <value>
        // /bradmin rp info <player>
        if (args.length < 2) {
            sender.sendMessage("§e使用法:");
            sender.sendMessage("  §f/bradmin rp set <player> <weapon> <value>");
            sender.sendMessage("  §f/bradmin rp info <player>");
            return true;
        }

        String rpSub = args[1].toLowerCase();

        if ("info".equals(rpSub)) {
            return handleRPInfo(sender, args);
        }

        if (!"set".equals(rpSub)) {
            sender.sendMessage("§e使用法: /bradmin rp set <player> <weapon> <value>");
            return true;
        }

        if (args.length < 5) {
            sender.sendMessage("§e使用法: /bradmin rp set <player> <weapon> <value>");
            sender.sendMessage("§7weapon: striker, gunner, marksman");
            return true;
        }

        String playerName = args[2];
        String weaponStr = args[3].toUpperCase();
        int value;

        try {
            value = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cRP値は数値で指定してください。");
            return true;
        }

        if (value < 0) {
            sender.sendMessage("§cRP値は0以上で指定してください。");
            return true;
        }

        WeaponType weaponType = WeaponType.fromString(weaponStr);
        if (weaponType == null) {
            sender.sendMessage("§c武器タイプが不正です。(striker, gunner, marksman)");
            return true;
        }

        // Find player UUID
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage("§cプレイヤー '" + playerName + "' がオンラインではありません。");
            return true;
        }

        UUID targetUuid = target.getUniqueId();
        BRBPlayer data = rankManager.getPlayer(targetUuid);
        if (data == null) {
            sender.sendMessage("§cプレイヤーデータが見つかりません。");
            return true;
        }

        String oldRank = data.getRankClass().getDisplayName();
        int oldRP = data.getWeaponRP(weaponType).getRp();

        rankManager.setWeaponRP(targetUuid, weaponType, value);

        String newRank = data.getRankClass().getDisplayName();

        sender.sendMessage("§a[Admin] §f" + playerName + " §7の §e" + weaponType.getDisplayName()
                + " §7RP: §f" + oldRP + " §7→ §a" + value);
        sender.sendMessage("§7ランク: §f" + oldRank + " §7→ §f" + newRank
                + " §7(総合RP: " + data.getTotalRP() + ")");

        // Notify the target player
        MessageUtil.sendInfo(target, "管理者により " + weaponType.getDisplayName()
                + " RPが §e" + value + " §7に設定されました。");

        return true;
    }

    private boolean handleRPInfo(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§e使用法: /bradmin rp info <player>");
            return true;
        }

        String playerName = args[2];
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage("§cプレイヤー '" + playerName + "' がオンラインではありません。");
            return true;
        }

        BRBPlayer data = rankManager.getPlayer(target.getUniqueId());
        if (data == null) {
            sender.sendMessage("§cプレイヤーデータが見つかりません。");
            return true;
        }

        sender.sendMessage("§6§l===== " + playerName + " RP情報 =====");
        sender.sendMessage("§fランク: " + data.getRankClass().getColoredName());
        sender.sendMessage("§f総合RP: §e" + data.getTotalRP());

        for (WeaponType wt : WeaponType.values()) {
            var wrp = data.getWeaponRP(wt);
            sender.sendMessage("  " + wt.getColor() + wt.getDisplayName()
                    + " §f: RP=" + wrp.getRp() + " W=" + wrp.getWins() + " L=" + wrp.getLosses());
        }

        sender.sendMessage("§6§l=========================");
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6/bradmin コマンド一覧:");
        sender.sendMessage("  §e/bradmin frame reload §7- frames.yml再読み込み");
        sender.sendMessage("  §e/bradmin forcestart §7- キュー強制開始");
        sender.sendMessage("  §e/bradmin rp set <player> <weapon> <value> §7- RP設定");
        sender.sendMessage("  §e/bradmin rp info <player> §7- RP情報表示");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("brb.admin")) return completions;

        if (args.length == 1) {
            completions.addAll(List.of("frame", "forcestart", "rp"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "frame" -> completions.add("reload");
                case "rp" -> completions.addAll(List.of("set", "info"));
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if ("rp".equals(sub)) {
                // Player name completion
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 4) {
            String sub = args[0].toLowerCase();
            String rpSub = args[1].toLowerCase();
            if ("rp".equals(sub) && "set".equals(rpSub)) {
                completions.addAll(List.of("striker", "gunner", "marksman"));
            }
        } else if (args.length == 5) {
            String sub = args[0].toLowerCase();
            String rpSub = args[1].toLowerCase();
            if ("rp".equals(sub) && "set".equals(rpSub)) {
                completions.addAll(List.of("1000", "3000", "5000", "10000", "15000"));
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix))
                .collect(Collectors.toList());
    }
}
