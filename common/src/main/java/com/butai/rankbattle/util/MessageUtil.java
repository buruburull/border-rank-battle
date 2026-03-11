package com.butai.rankbattle.util;

import org.bukkit.entity.Player;

public class MessageUtil {

    private static final String PREFIX = "§8[§6BRB§8] §r";

    public static void send(Player player, String message) {
        player.sendMessage(PREFIX + message);
    }

    public static void sendError(Player player, String message) {
        player.sendMessage(PREFIX + "§c" + message);
    }

    public static void sendSuccess(Player player, String message) {
        player.sendMessage(PREFIX + "§a" + message);
    }

    public static void sendWarning(Player player, String message) {
        player.sendMessage(PREFIX + "§e" + message);
    }

    public static void sendInfo(Player player, String message) {
        player.sendMessage(PREFIX + "§7" + message);
    }

    public static String prefix(String message) {
        return PREFIX + message;
    }
}
