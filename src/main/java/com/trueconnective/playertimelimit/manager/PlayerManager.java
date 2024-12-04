//********************************************************************************************
// Author:      TrueConnective, V01D-PH03N1X (PinguBasti)
// Project:     Player Time Limit
// Description: Kicks the player based on permissionGroup after a certain amount of time.
//********************************************************************************************
package com.trueconnective.playertimelimit.manager;

// Internal imports
import com.trueconnective.playertimelimit.handler.DatabaseHandler;
// Kyori Adventures imports
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
// Java Imports
import java.util.UUID;
import java.util.HashMap;

/**
 * This Class handles every interaction with the player
 */
public class PlayerManager {
    // Create DatabaseHandler object and set up a HashMap for the PlayerJoinTimes additionally it sets the maximal allowed playtime.
    private final DatabaseHandler dbHandler;
    private final HashMap<UUID, Long> playerJoinTimes = new HashMap<>();
    private final long maxAllowedTime = 3600; // Erste Version deshalb festgelegt auf 1 Stunde

    /**
     * On object creation set the DatabaseHandler
     */
    public PlayerManager(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    /**
     * Add the PlayerJoinTime at Player Join.
     */
    public void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        playerJoinTimes.put(uuid, System.currentTimeMillis());
    }

    /**
     * Sets immediately the timeSpent and stops the "Countdown" of playtime for the specific user.
     */
    public void handlePlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        long joinTime = playerJoinTimes.getOrDefault(uuid, System.currentTimeMillis());
        long timeSpent = (System.currentTimeMillis() - joinTime) / 1000;
        dbHandler.updatePlayerTime(uuid.toString(), timeSpent);
        playerJoinTimes.remove(uuid);
    }

    /**
     * Checks every Minute if any player has to be kicked. If any player has no playtime left this function kicks the player with a message.
     */
    public void checkAndKickPlayers(JavaPlugin plugin) {
        // Create a BukkitRunnable
        new BukkitRunnable() {
            // This function is executed after a specific given time.
            @Override
            public void run() {
                // Check every online Player.
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    long totalTime = dbHandler.getPlayerTime(uuid.toString()) +
                            (System.currentTimeMillis() - playerJoinTimes.getOrDefault(uuid, System.currentTimeMillis())) / 1000;

                    // If Playtime has been exceeded kick the Player with a message.
                    if (totalTime >= maxAllowedTime) {
                        TextComponent kickMessage = Component.text("Du hast heute das Limit deiner Spielzeit erreicht!\n").color(TextColor.color(0xff4433)).decorate(TextDecoration.BOLD)
                                        .append(Component.text("Du kannst in 24 Stunden wieder Spielen!").color(TextColor.color(0xefefef)).decoration(TextDecoration.BOLD, false));

                        Bukkit.getScheduler().runTask(plugin, () -> player.kick(kickMessage)); // Kick Player on Schedule with Message.
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 60); // Check every minute and run function asynchronous.
    }

    /**
     * Resets the Playtime of a specific Player.
     */
    public void resetPlayerPlayTime(OfflinePlayer player){
        UUID uuid = player.getUniqueId();
        dbHandler.resetPlayTime(uuid.toString());
    }
}
