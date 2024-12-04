//********************************************************************************************
// Author:      TrueConnective, V01D-PH03N1X (PinguBasti)
// Project:     Player Time Limit
// Description: Kicks the player based on permissionGroup after a certain amount of time.
//********************************************************************************************
package com.trueconnective.playertimelimit.commands;

import com.trueconnective.playertimelimit.PlayerTimeLimit;
import com.trueconnective.playertimelimit.manager.PlayerManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerTimeResetCommand extends BukkitCommand {
    private final PlayerManager playerManager = PlayerTimeLimit.playerManager;
    public PlayerTimeResetCommand() {
        super("playertimereset");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        // Check if target player is given
        if (args.length == 0 && !(sender instanceof Player)) {
            sender.sendMessage("Du musst einen Zielspieler angeben, wenn du diesen Befehl von der Konsole aus ausführst.");
            return true;
        }

        // set targetplayer
        OfflinePlayer targetPlayer = null;
        if (args.length > 0) {
            // Load player from argument
            targetPlayer = sender.getServer().getOfflinePlayer(args[0]);
            if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
                sender.sendMessage("Spieler \"" + args[0] + "\" wurde nicht gefunden oder hat den Server noch nie betreten.");
                return true;
            }
        } else if (sender instanceof Player player) {
            // No Argument sender is the Player
            targetPlayer = player;
        }

        if (targetPlayer == null) {
            sender.sendMessage("Du musst einen Zielspieler angeben.");
            return true;
        }

        // reset playtime
        playerManager.resetPlayerPlayTime(targetPlayer);

        // Output a response
        sender.sendMessage("Die Spielzeit von " + targetPlayer.getName() + " wurde zurückgesetzt.");
        return true;
    }
}
