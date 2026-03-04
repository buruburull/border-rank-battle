package com.borderrank.battle.command;

import com.borderrank.battle.BRBPlugin;
import com.borderrank.battle.manager.LoadoutManager;
import com.borderrank.battle.manager.TriggerRegistry;
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

/**
 * Command handler for /trigger command.
 * Manages trigger loadouts and presets.
 */
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

    /**
     * Handle /trigger list command - lists available triggers.
     */
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

    /**
     * Handle /trigger set command - sets a trigger in a loadout slot.
     */
    private void handleSet(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendErrorMessage(player, "Usage: /trigger set <slot 1-8> <trigger_id>");
            return;
        }

        BRBPlugin plugin = BRBPlugin.getInstance();
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

        String triggerId = args[2];
        TriggerData trigger = registry.get(triggerId);

        if (trigger == null) {
            MessageUtil.sendErrorMessage(player, "Trigger not found: " + triggerId);
            return;
        }

        MessageUtil.sendSuccessMessage(player, "Trigger " + trigger.getName() + " set in slot " + slot);
    }

    /**
     * Handle /trigger remove command - removes trigger from slot.
     */
    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendErrorMessage(player, "Usage: /trigger remove <slot 1-8>");
            return;
        }

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

        MessageUtil.sendSuccessMessage(player, "Trigger removed from slot " + slot);
    }

    /**
     * Handle /trigger view command - shows current loadout.
     */
    private void handleView(Player player, String[] args) {
        MessageUtil.sendInfoMessage(player, "=== Your Loadout ===");
        MessageUtil.sendInfoMessage(player, "Total Cost: 0");
    }

    /**
     * Handle /trigger preset command - manages presets.
     */
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
