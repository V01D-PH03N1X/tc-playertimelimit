//********************************************************************************************
// Author:      TrueConnective, V01D-PH03N1X (PinguBasti)
// Project:     Player Time Limit
// Description: Kicks the player based on permissionGroup after a certain amount of time.
//********************************************************************************************
package com.trueconnective.playertimelimit.handler;

// Internal import
import com.trueconnective.playertimelimit.PlayerTimeLimit;
// Database | SQLite imports
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * This Class handles every interaction with the Database.
 */
public class DatabaseHandler {
    // JDBC Connection Path
    private static final String DB_URL = "jdbc:sqlite:plugins/PlayerTimeLimit/database.db";

    /**
     * Connects to the given database and creates the player_time Table if it doesn't exist.
     */
    public void initializeDatabase(PlayerTimeLimit plugin) {

        // Get the plugin data directory
        File pluginFolder = plugin.getDataFolder(); // this is the default plugin directory

        // Create directory if it is not existing
        if (!pluginFolder.exists()) {
            if (pluginFolder.mkdirs()) {
                PlayerTimeLimit.logger.info("Creating Plugindirectory: {}", pluginFolder.getAbsolutePath());
            } else {
                PlayerTimeLimit.logger.error("Could't create plugin directory: {}", pluginFolder.getAbsolutePath());
                return; // Cancel if creating plugin directory fails.
            }
        }
        // Connect to the database
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create the SQL-Statement for Creating the database table if it doesn't exist.
            String createTableSQL = "CREATE TABLE IF NOT EXISTS player_time (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "time_spent INTEGER DEFAULT 0)";

            // Execute the statement in database.
            conn.createStatement().execute(createTableSQL);
        } catch (SQLException e) {
            // If any Error Occurs print it to the Console.
            PlayerTimeLimit.logger.error(e.getMessage());
        }
    }

    /**
     * Updates the database with the updated playtime of the specific player.
     */
    public void updatePlayerTime(String uuid, long timeSpent) {
        // Connect to database
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create SQL-Statement for updating spent time on the server.
            String updateSQL = "INSERT INTO player_time (uuid, time_spent) VALUES (?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET time_spent = time_spent + ?";

            // Prepare Statement and insert Arguments.
            PreparedStatement stmt = conn.prepareStatement(updateSQL);
            stmt.setString(1, uuid);
            stmt.setLong(2, timeSpent);
            stmt.setLong(3, timeSpent);
            // Execute SQL-Statement
            stmt.executeUpdate();
        } catch (SQLException e) {
            // If any Error Occurs print it to the Console.
            PlayerTimeLimit.logger.error(e.getMessage());
        }
    }

    /**
     * Returns the Playtime of the specific Player or if an error occurs the value 0.
     */
    public long getPlayerTime(String uuid) {
        // Connect to database.
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create SQL-Statement for gathering the time spent on the Server.
            String querySQL = "SELECT time_spent FROM player_time WHERE uuid = ?";
            // Prepare the SQL-Statement and insert Arguments.
            PreparedStatement stmt = conn.prepareStatement(querySQL);
            stmt.setString(1, uuid);
            // Execute SQL-Statement in a Query save result in variable 'rs'.
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // return the amount of time spent on the Server.
                return rs.getLong("time_spent");
            }
        } catch (SQLException e) {
            // If any Error Occurs print it to the Console.
            PlayerTimeLimit.logger.error(e.getMessage());
        }
        // return "0" if any Error Occurs to prevent a Server / Plugin crash.
        return 0;
    }

    /**
     * Resets the Playtime of every user.
     */
    public void resetDailyTimes() {
        // Connect to database.
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create SQL-Statement for resetting every player playtime.
            String resetSQL = "UPDATE player_time SET time_spent = 0";
            // Execute SQL-Statement.
            conn.createStatement().execute(resetSQL);
        } catch (SQLException e) {
            // If any Error Occurs print it to the Console.
            PlayerTimeLimit.logger.error(e.getMessage());
        }
    }

    /**
     * Resets the Playtime of a specific player
     */
    public void resetPlayTime(String uuid){
        // Connect to database.
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create SQL-Statement for resetting every player playtime.
            String resetSQL = "UPDATE player_time SET time_spent = 0 WHERE uuid = ?";
            PreparedStatement stmt = conn.prepareStatement(resetSQL);
            stmt.setString(1, uuid);
            // Execute SQL-Statement and resets playtime in the database.
            stmt.executeUpdate();
        } catch (SQLException e) {
            // If any Error Occurs print it to the Console.
            PlayerTimeLimit.logger.error(e.getMessage());
        }
    }
}
