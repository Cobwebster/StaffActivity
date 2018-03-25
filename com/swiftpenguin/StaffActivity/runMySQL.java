package com.swiftpenguin.staffactivity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class runMySQL implements Listener {

    private StaffActivity plugin;

    public runMySQL(StaffActivity plugin) {
        this.plugin = plugin;
    }

    public void configUpdater() {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT UUID, Timestamp, PlayerName FROM tracking");
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    do {
                        String uuid = results.getString("UUID");
                        String pname = results.getString("PlayerName");
                        long timestamp = results.getLong("Timestamp");

                        plugin.getConfig().set("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                        plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".pName", pname);
                        plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".status", "");
                        plugin.getConfig().options().copyDefaults(true);
                        plugin.saveConfig();
//                        System.out.println("Confg Loop Ran...");
                    } while (results.next());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 500L, 10000L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPermission("staff.log")) {
            return;
        }

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        long timestamp = System.currentTimeMillis() / 1000;

        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try { // MySQL/Config Creating a Player table + Duplication Prevention
                PreparedStatement check = plugin.getConnection().prepareStatement("SELECT UUID FROM tracking WHERE UUID = ?"); // Check if they exist in MySQL
                check.setString(1, uuid.toString());
                ResultSet rs = check.executeQuery();
                if (!rs.next()) {
                    PreparedStatement insert = plugin.getConnection().prepareStatement("INSERT INTO tracking (UUID,Timestamp,PlayerName) VALUE (?,?,?)"); // Adding them to MySQL
                    insert.setString(1, uuid.toString());
                    insert.setLong(2, System.currentTimeMillis() / 1000);
                    insert.setString(3, e.getPlayer().getName());
                    insert.executeUpdate();
                    insert.close();
//                    System.out.println("Added Player to MySQL");
//                    System.out.println("Prevented MySQL player duplication");
                }
            } catch (SQLException eve) {
                eve.printStackTrace();
            }

            if (!plugin.getConfig().getConfigurationSection("StaffUUIDS").getKeys(false).contains(uuid.toString())) {
                plugin.getConfig().addDefault("StaffUUIDS." + uuid, null);
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".status", "ACTIVE");
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".pName", player.getName());
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveConfig();
//                System.out.println("Added Player to Config");
            } else {
                plugin.getConfig().set("StaffUUIDS." + uuid + ".status", "ACTIVE");
                plugin.getConfig().set("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                plugin.getConfig().set("StaffUUIDS." + uuid + ".pName", player.getName());
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveConfig();
//                System.out.println("Updated Config TimeStamp");
                try { // Updating MySQL timestamp
                    PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE tracking SET Timestamp=? WHERE UUID = ?");
                    statement.setLong(1, timestamp);
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
//                    System.out.println("Updated MySQL Timestamp");
                } catch (SQLException eve) {
                    eve.printStackTrace();
                }
            }
        });
    }
}



