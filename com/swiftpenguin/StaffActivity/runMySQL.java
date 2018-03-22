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
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT UUID, Timestamp FROM tracking");
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    do {
                        String uuid = results.getString("UUID");
                        long timestamp = results.getLong("Timestamp");

                        plugin.getConfig().set("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                        plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".pName", "");
                        plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".status", "");
                        plugin.getConfig().options().copyDefaults(true);
                        plugin.saveConfig();
                        // Config Updater Loop - Keeps all servers up to date
                    } while (results.next());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 200L, 1000L);
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
            if (!plugin.getConfig().getConfigurationSection("StaffUUIDS").getKeys(false).contains(uuid.toString())) {
                plugin.getConfig().addDefault("StaffUUIDS." + uuid, null);
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".status", "ACTIVE");
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".pName", player.getName());
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveConfig();
                try { // MySQL Creating a Player table
                    PreparedStatement check = plugin.getConnection().prepareStatement("SELECT UUID FROM tracking WHERE UUID = ?"); // Check if they exist in MySQL
                    check.setString(1, uuid.toString());
                    ResultSet rs = check.executeQuery();
                    if (!rs.next()) {
                        PreparedStatement insert = plugin.getConnection().prepareStatement("INSERT INTO tracking (UUID,Timestamp) VALUE (?,?)"); // Adding them to MySQL
                        insert.setString(1, uuid.toString());
                        insert.setLong(2, System.currentTimeMillis() / 1000);
                        insert.executeUpdate();
                        // Player Added to MySQL + Config
                        // Prevent double MySQL logging between servers
                    }
                } catch (SQLException eve) {
                    eve.printStackTrace();
                }
            } else {
                plugin.getConfig().set("StaffUUIDS." + uuid + ".status", "ACTIVE");
                plugin.getConfig().set("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                plugin.getConfig().set("StaffUUIDS." + uuid + ".pName", player.getName());
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveConfig();
                try { // Updating MySQL timestamp
                    PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE tracking SET Timestamp=? WHERE UUID = ?");
                    statement.setLong(1, timestamp);
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    // Updated Player TimeStamp
                } catch (SQLException eve) {
                    eve.printStackTrace();
                }
            }
        });
    }
}

//Below this point is not used, was just for testing.... Will remove here shortly...


//
//    @EventHandler
//    public void onTalk (AsyncPlayerChatEvent e){
//        UUID uuid = e.getPlayer().getUniqueId();
//        try {
//            PreparedStatement check = plugin.getConnection().prepareStatement("SELECT UUID FROM tracking WHERE UUID = ?");
//            check.setString(1, uuid.toString());
//            ResultSet rs = check.executeQuery();
//            if (!rs.next()) {
//                System.out.println("Not Found Player");
//            } else {
//                System.out.println("Found Player");
//            }
//        } catch (SQLException eve){
//            eve.printStackTrace();
//        }
//    }


//    public boolean playerExists(UUID uuid) {
//        try {
//            PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM tracking WHERE UUID = ?");
//            statement.setString(1, uuid.toString());
//            ResultSet results = statement.executeQuery();
//            if (results.next()) {
//                Bukkit.getServer().broadcastMessage("FOUND");
//                return true;
//            }
//            Bukkit.getServer().broadcastMessage("FAILED");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public void createPlayer(final UUID uuid, Player player) {
//        try {
//            PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM tracking WHERE UUID = ?");
//            statement.setString(1, uuid.toString());
//            ResultSet results = statement.executeQuery();
//            results.next();
//            if (!playerExists(uuid)) {
//                PreparedStatement insert = plugin.getConnection().prepareStatement("INSERT INTO tracking (UUID,Status,Timestamp) VALUE (?,?,?)");
//                insert.setString(1, uuid.toString());
//                insert.setString(2, "Active");
//                insert.setLong(3, System.currentTimeMillis() / 1000);
//                insert.executeUpdate();
//                Bukkit.getServer().broadcastMessage("PLAYED ADDED");
//            } else {
//                Bukkit.getServer().broadcastMessage("PLAYED ALREADY EXISTS");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void getTimeStamp(UUID uuid) {
//        try {
//            PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM tracking WHERE UUID = ?");
//            statement.setString(1, uuid.toString());
//            ResultSet results = statement.executeQuery();
//            results.next();
//            System.out.println(results.getLong("Timestamp"));
//            System.out.println(results.getString("UUID"));
//            System.out.println(results.getString("Status"));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void updateTimestamp(UUID uuid, long timestamp) {
//        try {
//            PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE tracking SET Timestamp=? WHERE UUID = ?");
//            statement.setLong(1, timestamp);
//            statement.setString(2, uuid.toString());
//            statement.executeUpdate();
//            Bukkit.getServer().broadcastMessage("updated timestamp");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void mysqlMethod(UUID uuid, Player player) {
//        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
//        });
//    }
//}

//    public void loopMysql(UUID uuid) { //doesn't need uuid input
//        try {
//            PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT UUID FROM tracking");
//            ResultSet results = statement.executeQuery();
//            results.next();
//            do {
//                System.out.println(results.getString("UUID"));
//            } while (results.next());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }





