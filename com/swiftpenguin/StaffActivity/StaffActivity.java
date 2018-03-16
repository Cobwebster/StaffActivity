package com.swiftpenguin.StaffActivity;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.util.UUID;

public class StaffActivity extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        registerConfifg();

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {

                for (String uuid :getConfig().getConfigurationSection("StaffUUIDS").getKeys(false)) {
                    int LastTimeStamp = getConfig().getInt("StaffUUIDS." + uuid + ".timeStamp");
                    long CurrentTime = System.currentTimeMillis() / 1000;
                    long Difference = CurrentTime - LastTimeStamp;

                    if (Difference >= 432000) {
                        if (getConfig().getString("StaffUUIDS." + uuid + ".status") != "DEAD") {
                            getConfig().set("StaffUUIDS." + uuid + ".status", "DEAD");
                            getConfig().options().copyDefaults(true);
                            saveConfig();
                        }
                    } else if (Difference >= 259200) {
                        if (getConfig().getString("StaffUUIDS." + uuid + ".status") != "DANGER") {
                            getConfig().set("StaffUUIDS." + uuid + ".status", "DANGER");
                            getConfig().options().copyDefaults(true);
                            saveConfig();
                        }
                    } else if (Difference >= 172800) {
                        if (getConfig().getString("StaffUUIDS." + uuid + ".status") != "INACTIVE") {
                            getConfig().set("StaffUUIDS." + uuid + ".status", "INACTIVE");
                            getConfig().options().copyDefaults(true);
                            saveConfig();
                        } else if (Difference <= 86400) {
                            if (getConfig().getString("StaffUUIDS." + uuid + ".status") != "ACTIVE") {
                                getConfig().set("StaffUUIDS." + uuid + ".status", "ACTIVE");
                                getConfig().options().copyDefaults(true);
                                saveConfig();
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        }, 100, 12000);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("staffcheck") && sender instanceof Player) {
            if (sender.hasPermission("staffcheck.use")) {

                long timestamp = System.currentTimeMillis() / 1000;

                for (String uuid : getConfig().getConfigurationSection("StaffUUIDS").getKeys(false)) {

                    int time = getConfig().getInt("StaffUUIDS." + uuid + ".timeStamp");
                    long calc = (timestamp - time) / 60;

                    if (getConfig().getString("StaffUUIDS." + uuid + ".status").equalsIgnoreCase("DEAD")) {
                        sender.sendMessage(" Status: " + getConfig().getString("StaffUUIDS." + uuid + ".pName") + "  > " + ChatColor.DARK_RED + getConfig().getString("StaffUUIDS." + uuid + ".status")+ChatColor.RESET + " Mins Since LOGIN > " +  calc);
                    } else if (getConfig().getString("StaffUUIDS." + uuid + ".status").equalsIgnoreCase("INACTIVE")) {
                        sender.sendMessage(" Status: " + getConfig().getString("StaffUUIDS." + uuid + ".pName") + "  > " + ChatColor.RED + getConfig().getString("StaffUUIDS." + uuid + ".status")+ChatColor.RESET + " Mins Since LOGIN > " +  calc);
                    } else if (getConfig().getString("StaffUUIDS." + uuid + ".status").equalsIgnoreCase("ACTIVE")) {
                        sender.sendMessage(" Status: " + getConfig().getString("StaffUUIDS." + uuid + ".pName") + "  > " + ChatColor.GREEN + getConfig().getString("StaffUUIDS." + uuid + ".status")+ChatColor.RESET + " Mins Since LOGIN > " +  calc);
                    } else if (getConfig().getString("StaffUUIDS." + uuid + ".status").equalsIgnoreCase("DANGER")) {
                        sender.sendMessage(" Status: " + getConfig().getString("StaffUUIDS." + uuid + ".pName") + "  > " + ChatColor.GREEN + getConfig().getString("StaffUUIDS." + uuid + ".status")+ChatColor.RESET + " Mins Since LOGIN > " +  calc);
                        {

                        }
                    }
                }
            }
        }
        return true;
    }

    private void registerConfifg(){
        saveDefaultConfig();
    }

    @EventHandler
    public void onJoin (PlayerJoinEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.hasPermission("staff.log")) {
            if (!getConfig().getConfigurationSection("StaffUUIDS").getKeys(false).contains(uuid.toString())) {

                long timestamp = System.currentTimeMillis() / 1000;
                    getConfig().addDefault("StaffUUIDS." + uuid, null);
                    getConfig().addDefault("StaffUUIDS." + uuid + ".status", "ACTIVE");
                    getConfig().addDefault("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                    getConfig().addDefault("StaffUUIDS." + uuid + ".pName", player.getName());
                    getConfig().options().copyDefaults(true);
                    saveConfig();
//                long timestamp = System.currentTimeMillis() / 1000;
//                getConfig().set("StaffUUIDS." + uuid + ".timeStamp", timestamp);
//                getConfig().options().copyDefaults(true);
//                saveConfig();
            } else {
                long timestamp = System.currentTimeMillis() / 1000;
                    getConfig().set("StaffUUIDS." + uuid, null);
                    getConfig().set("StaffUUIDS." + uuid + ".status", "ACTIVE");
                    getConfig().set("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                    getConfig().set("StaffUUIDS." + uuid + ".pName", player.getName());
                    getConfig().options().copyDefaults(true);
                    saveConfig();
            }
        }
    }
}
