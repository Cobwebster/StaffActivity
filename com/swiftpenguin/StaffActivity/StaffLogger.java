package com.swiftpenguin.staffactivity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class StaffLogger implements Listener {

    private StaffActivity plugin;

    public StaffLogger(StaffActivity plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.hasPermission("staff.log")) {
            if (!plugin.getConfig().getConfigurationSection("StaffUUIDS").getKeys(false).contains(uuid.toString())) {

                long timestamp = System.currentTimeMillis() / 1000;
                plugin.getConfig().addDefault("StaffUUIDS." + uuid, null);
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".status", "ACTIVE");
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                plugin.getConfig().addDefault("StaffUUIDS." + uuid + ".pName", player.getName());
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveConfig();
            } else {
                long timestamp = System.currentTimeMillis() / 1000;
                plugin.getConfig().set("StaffUUIDS." + uuid, null);
                plugin.getConfig().set("StaffUUIDS." + uuid + ".status", "ACTIVE");
                plugin.getConfig().set("StaffUUIDS." + uuid + ".timeStamp", timestamp);
                plugin.getConfig().set("StaffUUIDS." + uuid + ".pName", player.getName());
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveConfig();
            }
        }
    }
}
