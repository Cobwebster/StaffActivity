package com.swiftpenguin.staffactivity;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class StaffActivity extends JavaPlugin implements Listener {

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private Connection connection;
    public String host, database, username, password, table;
    public int port;

    public void mysqlSetup() {
        host = getConfig().getString("MySQL.IP");
        port = getConfig().getInt("MySQL.Port");
        username = getConfig().getString("MySQL.Username");
        database = getConfig().getString("MySQL.DB-Name");
        password = getConfig().getString("MySQL.Password");
        table = getConfig().getString("MySQL.Table");

        try {
            synchronized (this) {
                if (getConnection() != null && !getConnection().isClosed()) {
                    return;
                }
                setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password));
                System.out.println("StaffActivity MySQL Connected.");
                try {
                    PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `tracking` (`UUID` varchar(65), `Timestamp` varchar(65), `PlayerName` varchar(65))");
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        registerConfig();

        if (getConfig().getBoolean("MySQL.UseMySQL")) {
            getServer().getPluginManager().registerEvents(new runMySQL(this), this);
            mysqlSetup();
            runMySQL sqltask = new runMySQL(this);
            sqltask.configUpdater();
        } else {
            System.out.println("StaffActivity MySQL Mode Disabled, using local data...");
            getServer().getPluginManager().registerEvents(new StaffLogger(this), this);
        }

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
            for (String uuid : getConfig().getConfigurationSection("StaffUUIDS").getKeys(false)) {
                int LastTimeStamp = getConfig().getInt("StaffUUIDS." + uuid + ".timeStamp");
                long CurrentTime = System.currentTimeMillis() / 1000;
                long difference = CurrentTime - LastTimeStamp;

                if (difference <= 86400) {

                    getConfig().set("StaffUUIDS." + uuid + ".status", "ACTIVE");
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                }
                if (difference >= 172800 && difference < 259200) {

                    getConfig().set("StaffUUIDS." + uuid + ".status", "INACTIVE");
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                }
                if (difference >= 259200 && difference < 432000) {
                    getConfig().set("StaffUUIDS." + uuid + ".status", "DANGER");
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                }
                if (difference >= 432000) {
                    getConfig().set("StaffUUIDS." + uuid + ".status", "DEAD");
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                }
            }
            }
        }, 800L, 12000);
    }

    private void registerConfig() {
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("staffcheck") && sender instanceof Player) {
            if (sender.hasPermission("staffcheck.use")) {

                long timestamp = System.currentTimeMillis() / 1000;
                int total = 0;
                int dead = 0;
                int inactive = 0;
                int danger = 0;
                int active = 0;

                    for (String uuid : getConfig().getConfigurationSection("StaffUUIDS").getKeys(false)) {
                        int time = getConfig().getInt("StaffUUIDS." + uuid + ".timeStamp");
                        long calc = (timestamp - time) / 60;
                        long calcd = calc / 60;
                        total++;

                        if (getConfig().getString("StaffUUIDS." + uuid + ".status").equalsIgnoreCase("DEAD")) {
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + getConfig().getString("StaffUUIDS." + uuid + ".pName") + ChatColor.DARK_GRAY + " -> " + ChatColor.DARK_RED + getConfig().getString("StaffUUIDS." + uuid + ".status") + ChatColor.DARK_GRAY + " -> " + ChatColor.GOLD + calcd);
                            dead++;
                        } else if (getConfig().getString("StaffUUIDS." + uuid + ".status").equalsIgnoreCase("INACTIVE")) {
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + getConfig().getString("StaffUUIDS." + uuid + ".pName") + ChatColor.DARK_GRAY + " -> " + ChatColor.RED + getConfig().getString("StaffUUIDS." + uuid + ".status") + ChatColor.DARK_GRAY + " -> " + ChatColor.GOLD + calcd);
                            inactive++;
                        } else if (getConfig().getString("StaffUUIDS." + uuid + ".status").equalsIgnoreCase("ACTIVE")) {
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + getConfig().getString("StaffUUIDS." + uuid + ".pName") + ChatColor.DARK_GRAY + " -> " + ChatColor.GREEN + getConfig().getString("StaffUUIDS." + uuid + ".status") + ChatColor.DARK_GRAY + " -> " + ChatColor.GOLD + calcd);
                            active++;
                        } else if (getConfig().getString("StaffUUIDS." + uuid + ".status").equalsIgnoreCase("DANGER")) {
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + getConfig().getString("StaffUUIDS." + uuid + ".pName") + ChatColor.DARK_GRAY + " -> " + ChatColor.RED + getConfig().getString("StaffUUIDS." + uuid + ".status") + ChatColor.DARK_GRAY + " -> " + ChatColor.GOLD + calcd);
                            danger++;
                        }
                    }
                    sender.sendMessage(ChatColor.GOLD + "Monitoring " + total + " Active " + active + " Inactive " + inactive + " Danger " + danger + " Dead " + dead);
            }
        }
            return true;
        }
}
