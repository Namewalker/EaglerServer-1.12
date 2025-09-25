package com.shadowlord.eaglerlogin;

import com.shadowlord.eaglerlogin.commands.LoginCommand;
import com.shadowlord.eaglerlogin.commands.RegisterCommand;
import com.shadowlord.eaglerlogin.listeners.LoginListener;
import com.shadowlord.eaglerlogin.listeners.ProtectionListener;
import org.bukkit.plugin.java.JavaPlugin;

public class EaglerLoginPlugin extends JavaPlugin {
  private LoginListener loginListener;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    loginListener = new LoginListener(this);
    getServer().getPluginManager().registerEvents(loginListener, this);
    getServer().getPluginManager().registerEvents(new ProtectionListener(loginListener, this), this);

    if (getCommand("register") != null) getCommand("register").setExecutor(new RegisterCommand(loginListener, this));
    if (getCommand("login") != null) getCommand("login").setExecutor(new LoginCommand(loginListener));

    getLogger().info("EaglerLogin enabled");
  }

  @Override
  public void onDisable() {
    if (loginListener != null) loginListener.saveUsers();
    getLogger().info("EaglerLogin disabled");
  }
}
