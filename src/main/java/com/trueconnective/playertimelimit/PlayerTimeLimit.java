//********************************************************************************************
// Author:      TrueConnective, V01D-PH03N1X (PinguBasti)
// Project:     Player Time Limit
// Description: Kicks the player based on permissionGroup after a certain amount of time.
//********************************************************************************************
package com.trueconnective.playertimelimit;

// Internal Imports
import com.trueconnective.playertimelimit.commands.ResetPlayTimeCommand;
import com.trueconnective.playertimelimit.handler.DatabaseHandler;
import com.trueconnective.playertimelimit.manager.PlayerManager;
// Kyori Adveture Imports
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
// Bukkit Imports
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

/**
 * The Plugin MainClass here are Events Located.
 */
public class PlayerTimeLimit extends JavaPlugin implements Listener {
    public static PlayerManager playerManager;
    public static Logger logger;

    @Override
    public void onEnable() {
        // Get and Set the plugin Logger.
        logger = getSLF4JLogger();

        // Create DatabaseHandler for Database connectivity. And initialize Database (Create Table if not Exists)
        DatabaseHandler dbHandler = new DatabaseHandler();
        dbHandler.initializeDatabase(this);

        // Create PlayerManager Object for handling Join & Quit Event and Kick Action after the maximum playing time is reached.
        playerManager = new PlayerManager(dbHandler);

        // register every used Event.
        getServer().getPluginManager().registerEvents(this, this);
        // register created commands
        getCommand("playtimereset").setExecutor(new ResetPlayTimeCommand());

        // Start the Check for the player Kick
        playerManager.checkAndKickPlayers(this);

        // Reset timestamp daily
        dbHandler.resetDailyTimes();
        getServer().getScheduler().runTaskTimerAsynchronously(this, dbHandler::resetDailyTimes, 0L, 20L * 60 * 60 * 24);
    }

    @Override
    public void onDisable() {
        // If the plugin is deactivated, every player is treated as if they had left the server.
        getServer().getOnlinePlayers().forEach(playerManager::handlePlayerQuit);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // On player join create the entry for the Player in the database
        playerManager.handlePlayerJoin(event.getPlayer());

        // Send player welcome message from our plugin to the joined player.
        final TextComponent welcomeMessage1 = Component.text()
                .content("Willkommen auf dem ")
                .color(TextColor.color(0xefefef))
                .append(Component.text().content("TrueConnective ").decoration(TextDecoration.BOLD, true).clickEvent(ClickEvent.openUrl("https://trueconnective.com")))
                .append(Component.text("Server!").decoration(TextDecoration.BOLD, false))
                .build();


        event.getPlayer().sendMessage(welcomeMessage1);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Handle the player quit event and stop counting the remaining playertime.
        playerManager.handlePlayerQuit(event.getPlayer());
    }
}
