package com.borderrank.battle.command;

import com.borderrank.battle.BRBPlugin;
import com.borderrank.battle.manager.RankManager;
import com.borderrank.battle.model.BRBPlayer;
import com.borderrank.battle.model.Team;
import com.borderrank.battle.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command handler for /team command.
 * Manages team creation, invitations, and membership.
 */
public class TeamCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.sendInfoMessage(player, "Usage: /team <create|invite|accept|deny|leave|info>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "create" -> handleCreate(player, args);
            case "invite" -> handleInvite(player, args);
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
            case "leave" -> handleLeave(player);
            case "info" -> handleInfo(player, args);
            default -> MessageUtil.sendErrorMessage(player, "Unknown subcommand: " + subcommand);
        }

        return true;
    }

    /**
     * Handle /team create command - creates a new team (requires B rank or higher).
     */
    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendErrorMessage(player, "Usage: /team create <name>");
            return;
        }

        BRBPlugin plugin = BRBPlugin.getInstance();
        RankManager rankManager = plugin.getRankManager();
        BRBPlayer brPlayer = rankManager.getPlayer(player.getUniqueId());

        if (brPlayer == null) {
            MessageUtil.sendErrorMessage(player, "Your player data was not found.");
            return;
        }

        // Check if already in a team
        if (rankManager.getPlayerTeam(player.getUniqueId()) != null) {
            MessageUtil.sendErrorMessage(player, "既にチームに所属しています！先に /team leave してください。");
            return;
        }

        // Check rank requirement (B rank or higher)
        String rankTier = rankManager.getHighestRankTier(brPlayer);
        if (!isRankBOrHigher(rankTier)) {
            MessageUtil.sendErrorMessage(player, "チーム作成にはBランク以上が必要です！");
            return;
        }

        String teamName = args[1];
        Team team = new Team(teamName, player.getUniqueId());

        if (rankManager.createTeam(team)) {
            MessageUtil.sendSuccessMessage(player, "チーム '" + teamName + "' を作成しました！");
        } else {
            MessageUtil.sendErrorMessage(player, "そのチーム名は既に使われています！");
        }
    }

    /**
     * Handle /team invite command - sends an invitation to a player.
     */
    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendErrorMessage(player, "Usage: /team invite <player>");
            return;
        }

        BRBPlugin plugin = BRBPlugin.getInstance();
        RankManager rankManager = plugin.getRankManager();

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            MessageUtil.sendErrorMessage(player, "プレイヤーが見つかりません: " + args[1]);
            return;
        }

        Team team = rankManager.getPlayerTeam(player.getUniqueId());
        if (team == null) {
            MessageUtil.sendErrorMessage(player, "チームに所属していません！");
            return;
        }

        if (!team.getLeaderId().equals(player.getUniqueId())) {
            MessageUtil.sendErrorMessage(player, "リーダーのみ招待できます！");
            return;
        }

        // Check if target is already in a team
        if (rankManager.getPlayerTeam(targetPlayer.getUniqueId()) != null) {
            MessageUtil.sendErrorMessage(player, targetPlayer.getName() + " は既に別のチームに所属しています！");
            return;
        }

        // Send pending invite
        rankManager.addPendingInvite(targetPlayer.getUniqueId(), team.getName());
        MessageUtil.sendSuccessMessage(player, targetPlayer.getName() + " に招待を送りました！");
        MessageUtil.sendInfoMessage(targetPlayer, "§e" + player.getName() + " §fからチーム §b" + team.getName() + " §fへの招待が届きました！");
        MessageUtil.sendInfoMessage(targetPlayer, "§a/team accept §fで承諾、§c/team deny §fで拒否");
    }

    /**
     * Handle /team accept command - accepts a pending invitation.
     */
    private void handleAccept(Player player) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        RankManager rankManager = plugin.getRankManager();

        // Check if already in a team
        if (rankManager.getPlayerTeam(player.getUniqueId()) != null) {
            MessageUtil.sendErrorMessage(player, "既にチームに所属しています！先に /team leave してください。");
            return;
        }

        String teamName = rankManager.consumePendingInvite(player.getUniqueId());
        if (teamName == null) {
            MessageUtil.sendErrorMessage(player, "招待がありません！");
            return;
        }

        Team team = rankManager.getTeamByName(teamName);
        if (team == null) {
            MessageUtil.sendErrorMessage(player, "チームが存在しません！");
            return;
        }

        team.addMember(player.getUniqueId());
        rankManager.registerPlayerTeam(player.getUniqueId(), team.getName());
        MessageUtil.sendSuccessMessage(player, "チーム §b" + team.getName() + " §aに参加しました！");

        // Notify team leader
        Player leader = Bukkit.getPlayer(team.getLeaderId());
        if (leader != null) {
            MessageUtil.sendSuccessMessage(leader, player.getName() + " がチームに参加しました！");
        }
    }

    /**
     * Handle /team deny command - denies a pending invitation.
     */
    private void handleDeny(Player player) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        RankManager rankManager = plugin.getRankManager();

        String teamName = rankManager.consumePendingInvite(player.getUniqueId());
        if (teamName == null) {
            MessageUtil.sendErrorMessage(player, "招待がありません！");
            return;
        }

        MessageUtil.sendInfoMessage(player, "チーム §b" + teamName + " §fへの招待を拒否しました。");

        // Notify team leader
        Team team = rankManager.getTeamByName(teamName);
        if (team != null) {
            Player leader = Bukkit.getPlayer(team.getLeaderId());
            if (leader != null) {
                MessageUtil.sendErrorMessage(leader, player.getName() + " が招待を拒否しました。");
            }
        }
    }

    /**
     * Handle /team leave command - removes player from their team.
     */
    private void handleLeave(Player player) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        RankManager rankManager = plugin.getRankManager();

        Team team = rankManager.getPlayerTeam(player.getUniqueId());
        if (team == null) {
            MessageUtil.sendErrorMessage(player, "チームに所属していません！");
            return;
        }

        if (team.removeMember(player.getUniqueId())) {
            plugin.getRankManager().unregisterPlayerTeam(player.getUniqueId());
            MessageUtil.sendSuccessMessage(player, "チームを脱退しました。");

            // If team is now empty, delete team
            if (team.getMembers().isEmpty()) {
                rankManager.deleteTeam(team.getName());
            } else {
                // Notify leader
                Player leader = Bukkit.getPlayer(team.getLeaderId());
                if (leader != null) {
                    MessageUtil.sendInfoMessage(leader, player.getName() + " がチームを脱退しました。");
                }
            }
        } else {
            // Leader tried to leave - must disband or transfer
            MessageUtil.sendErrorMessage(player, "リーダーは脱退できません。チームを解散するには全メンバーを外してください。");
        }
    }

    /**
     * Handle /team info command - shows team information.
     */
    private void handleInfo(Player player, String[] args) {
        BRBPlugin plugin = BRBPlugin.getInstance();
        RankManager rankManager = plugin.getRankManager();

        Team team;
        if (args.length > 1) {
            team = rankManager.getTeamByName(args[1]);
        } else {
            team = rankManager.getPlayerTeam(player.getUniqueId());
        }

        if (team == null) {
            MessageUtil.sendErrorMessage(player, "チームが見つかりません。");
            return;
        }

        MessageUtil.sendInfoMessage(player, "§b=== チーム: " + team.getName() + " ===");
        MessageUtil.sendInfoMessage(player, "§eリーダー: §f" + Bukkit.getOfflinePlayer(team.getLeaderId()).getName());
        MessageUtil.sendInfoMessage(player, "§eメンバー数: §f" + team.getMembers().size());

        // List members
        StringBuilder memberList = new StringBuilder();
        for (UUID memberId : team.getMembers()) {
            if (memberList.length() > 0) {
                memberList.append("§f, ");
            }
            String name = Bukkit.getOfflinePlayer(memberId).getName();
            if (memberId.equals(team.getLeaderId())) {
                memberList.append("§e").append(name).append("§6[L]");
            } else {
                memberList.append("§a").append(name);
            }
        }
        MessageUtil.sendInfoMessage(player, "§eメンバー: " + memberList);
    }

    /**
     * Check if a rank is B or higher.
     */
    private boolean isRankBOrHigher(String rankTier) {
        return rankTier.equals("S") || rankTier.equals("A") || rankTier.equals("B");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("invite");
            completions.add("accept");
            completions.add("deny");
            completions.add("leave");
            completions.add("info");
        } else if (args.length == 2) {
            if ("invite".equalsIgnoreCase(args[0])) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    completions.add(onlinePlayer.getName());
                }
            } else if ("info".equalsIgnoreCase(args[0])) {
                BRBPlugin plugin = BRBPlugin.getInstance();
                RankManager rankManager = plugin.getRankManager();
                completions.addAll(rankManager.getAllTeamNames());
            }
        }

        return completions;
    }
}
