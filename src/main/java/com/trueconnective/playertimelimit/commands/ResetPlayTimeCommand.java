package com.trueconnective.playertimelimit.commands;

import com.trueconnective.playertimelimit.PlayerTimeLimit;
import com.trueconnective.playertimelimit.manager.PlayerManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResetPlayTimeCommand implements CommandExecutor {
    private PlayerManager playerManager = PlayerTimeLimit.playerManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Überprüfen, ob ein Zielspieler angegeben wurde
        if (args.length == 0 && !(sender instanceof Player)) {
            sender.sendMessage("Du musst einen Zielspieler angeben, wenn du diesen Befehl von der Konsole aus ausführst.");
            return true;
        }

        // Zielspieler bestimmen
        OfflinePlayer targetPlayer = null;
        if (args.length > 0) {
            // Spieler aus dem Argument laden (online oder offline)
            targetPlayer = sender.getServer().getOfflinePlayer(args[0]);
            if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
                sender.sendMessage("Spieler \"" + args[0] + "\" wurde nicht gefunden oder hat den Server noch nie betreten.");
                return true;
            }
        } else if (sender instanceof Player player) {
            // Kein Argument, Sender ist der Spieler
            targetPlayer = player;
        }

        if (targetPlayer == null) {
            sender.sendMessage("Du musst einen Zielspieler angeben.");
            return true;
        }

        // Spielerzeit zurücksetzen
        playerManager.resetPlayerPlayTime(targetPlayer);

        // Bestätigung ausgeben
        sender.sendMessage("Die Spielzeit von " + targetPlayer.getName() + " wurde zurückgesetzt.");
        return true;
    }

}
