package com.shadowlord.eaglerlogin;

import com.shadowlord.eaglerlogin.commands.*;
import com.shadowlord.eaglerlogin.listeners.LoginListener;
import com.shadowlord.eaglerlogin.listeners.ProtectionListener;
import com.shadowlord.eaglerlogin.util.BypassManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class EaglerLoginPlugin extends JavaPlugin {
  private LoginListener loginListener;
  private BypassManager bypassManager;

  @Override
  public void onEnable() {
    saveDefaultConfig();

    // instantiate core components
    loginListener = new LoginListener(this);
    getServer().getPluginManager().registerEvents(loginListener, this);

    // protection listener uses loginListener
    getServer().getPluginManager().registerEvents(new ProtectionListener(loginListener, this), this);

    // commands that rely on loginListener
    registerCommand("register", new RegisterCommand(loginListener, this));
    registerCommand("login", new LoginCommand(loginListener));

    // bypass manager and commands
    bypassManager = new BypassManager(this);
    registerCommand("bypasscreate", new BypassCreateCommand(bypassManager));
    registerCommand("bypasslogin", new BypassLoginCommand(bypassManager, loginListener, this));
    registerCommand("grantbypass", new GrantBypassCommand(this));

    // admin utilities
    registerCommand("setpassword", new AdminCommands(loginListener, this));
    registerCommand("resetpassword", new AdminCommands(loginListener, this));
    registerCommand("showhash", new AdminCommands(loginListener, this));

    getLogger().info("EaglerLogin enabled");
  }

  private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
    PluginCommand cmd = getCommand(name);
    if (cmd == null) {
      getLogger().warning("Command '" + name + "' not found in plugin.yml");
      return;
    }
    cmd.setExecutor(executor);
    getLogger().info("Registered command: /" + name);
  }

  @Override
  public void onDisable() {
    if (loginListener != null) loginListener.saveUsers();
    getLogger().info("EaglerLogin disabled");
  }

  public LoginListener getLoginListener() {
    return loginListener;
  }
}
