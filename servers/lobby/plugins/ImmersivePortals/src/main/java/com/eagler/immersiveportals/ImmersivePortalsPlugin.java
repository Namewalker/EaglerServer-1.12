package com.eagler.immersiveportals;

import org.bukkit.plugin.java.JavaPlugin;

public class ImmersivePortalsPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("ImmersivePortals enabled!");
        PortalManager portalManager = new PortalManager(this);
        getServer().getPluginManager().registerEvents(portalManager, this);
    getCommand("linkportal").setExecutor(new LinkPortalCommand(portalManager));
    getCommand("setportal").setExecutor(new SetPortalCommand(portalManager));
    getCommand("portalkill").setExecutor(new PortalKillCommand(portalManager));
    getCommand("portalname").setExecutor(new PortalNameCommand(portalManager));
    }

    @Override
    public void onDisable() {
        getLogger().info("ImmersivePortals disabled!");
    }
}
