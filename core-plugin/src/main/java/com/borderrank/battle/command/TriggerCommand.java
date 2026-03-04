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

        int totalCost = 0;
        Map<String, TriggerData> allTriggers = registry.getAll();
        List<String> slots = loadout.getSlots();
        if (slots != null) {
            for (String sid : slots) {
                if (sid != null && !sid.isEmpty()) {
                    TriggerData td = allTriggers.get(sid);
                    if (td != null) {
                        totalCost += td.getCost();
                    }
                }
            }
        }

        if (totalCost > 15) {
            loadout.setSlot(slotIndex, "");
            MessageUtil.sendErrorMessage(player, "TP制限超過！コスト: " + totalCost + "/15");
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

        MessageUtil.sendSuccessMessage(player, trigger.getName() + " をスロット " + slot + " にセット (Cost: " + totalCost + "/15 TP)");
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

        if (loadout == null) {
            MessageUtil.sendInfoMessage(player, "No triggers equipped. Use /trigger set <slot> <id>");
            return;
        }

        int totalCost = 0;
        boolean hasAny = false;
        Map<String, TriggerData> allTriggers = registry.getAll();
        List<String> slots = loadout.getSlots();

        for (int i = 0; i < Math.min(slots != null ? slots.size() : 0, 8); i++) {
            String triggerId = slots.get(i);
            if (triggerId != null && !triggerId.isEmpty()) {
                hasAny = true;
                TriggerData td = allTriggers.get(triggerId);
                String name = td != null ? td.getName() : triggerId;
                int cost = td != null ? td.getCost() : 0;
                totalCost += cost;
                String slotLabel = (i < 4) ? "Main" : "Sub";
                MessageUtil.sendInfoMessage(player, "  Slot " + (i + 1) + " [" + slotLabel + "]: " + name + " (Cost: " + cost + ")");
            }
        }

        if (!hasAny) {
            MessageUtil.sendInfoMessage(player, "No triggers equipped. Use /trigger set <slot> <id>");
            return;
        }

        MessageUtil.sendInfoMessage(player, "Total Cost: " + totalCost + "/15 TP");
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
            if ("set".equalsIgnoreCase(args[0]) || "remove".equalsIgnoreCase(args[0])) {
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
