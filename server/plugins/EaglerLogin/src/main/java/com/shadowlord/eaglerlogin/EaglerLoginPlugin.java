package com.shadowlord.eaglerlogin;

import com.shadowlord.eaglerlogin.commands.LoginCommand;
import com.shadowlord.eaglerlogin.commands.RegisterCommand;
import com.shadowlord.eaglerlogin.listeners.PlayerListener;
import com.shadowlord.eaglerlogin.listeners.DisconnectListener;
import org.bukkit.plugin.java.JavaPlugin;

public class EaglerLoginPlugin extends JavaPlugin {
  private UserManager users;
  private SessionManager sessions;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    users = new UserManager(this);
    sessions = new SessionManager();

    getCommand("register").setExecutor(new RegisterCommand(users));
    getCommand("login").setExecutor(new LoginCommand(users, sessions));

    getServer().getPluginManager().registerEvents(new PlayerListener(sessions), this);
    getServer().getPluginManager().registerEvents(new DisconnectListener(sessions), this);

    getLogger().info("EaglerLogin enabled");
  }

  @Override
  public void onDisable() {
    users.saveAll();
    getLogger().info("EaglerLogin disabled");
  }

  public UserManager getUserManager() {
    return users;
  }

  public SessionManager getSessionManager() {
    return sessions;
  }
}
