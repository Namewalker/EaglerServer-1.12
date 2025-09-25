package com.shadowlord.eaglerlogin;

import com.shadowlord.eaglerlogin.commands.LoginCommand;
import com.shadowlord.eaglerlogin.commands.RegisterCommand;
import com.shadowlord.eaglerlogin.listeners.LoginListener;
import com.shadowlord.eaglerlogin.listeners.ProtectionListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;

public class EaglerLoginPlugin extends JavaPlugin {
  private LoginListener loginListener;

  @Override
  public void onEnable() {
    saveDefaultConfig();

    // create & register listeners
    loginListener = new LoginListener(this);
    getServer().getPluginManager().registerEvents(loginListener, this);
    getServer().getPluginManager().registerEvents(new ProtectionListener(loginListener, this), this);

    // register commands safely with logging
    registerCommand("register", new RegisterCommand(loginListener, this));
    registerCommand("login", new LoginCommand(loginListener));

    getLogger().info("EaglerLogin enabled");
  }

  private void registerCommand(String name, org.bukkit.command.CommandExecutor exec) {
    PluginCommand cmd = getCommand(name);
    if (cmd == null) {
      getLogger().warning("Command '" + name + "' not found in plugin.yml. It will not be available.");
      return;
    }
    cmd.setExecutor(exec);
    getLogger().info("Registered command: /" + name);
  }

  @Override
  public void onDisable() {
    if (loginListener != null) loginListener.saveUsers();
    getLogger().info("EaglerLogin disabled");
  }
}
