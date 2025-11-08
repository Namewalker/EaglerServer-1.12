package com.shadowlord.macewind;

import com.shadowlord.macewind.listeners.MaceListener;
import com.shadowlord.macewind.listeners.WindChargeListener;
import com.shadowlord.macewind.listeners.FireworkBoostListener;
import org.bukkit.plugin.java.JavaPlugin;

public class MaceWindPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new MaceListener(this), this);
        getServer().getPluginManager().registerEvents(new WindChargeListener(this), this);
        getServer().getPluginManager().registerEvents(new FireworkBoostListener(this), this);

        getLogger().info("MaceWind enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MaceWind disabled.");
    }
}
