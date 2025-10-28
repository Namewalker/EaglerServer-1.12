package com.eagler.bookportal;

import com.eagler.bookportal.listeners.BookPortalListener;
import com.eagler.bookportal.dimension.DimensionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BookPortalPlugin extends JavaPlugin {

    private DimensionManager dimensionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.dimensionManager = new DimensionManager(this);
        getServer().getPluginManager().registerEvents(new BookPortalListener(this, dimensionManager), this);
        getLogger().info("BookPortal enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("BookPortal disabled");
    }

}
