package com.shadowlord.essentialslite;

import com.shadowlord.essentialslite.commands.*;
import com.shadowlord.essentialslite.storage.YamlStore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsLite extends JavaPlugin {
  private YamlStore store;
  private ModerationCommand moderation;

  @Override
  public void onEnable() {
    // Initialize data store
    store = new YamlStore(this, "essentials.yml");
    store.load();

    // Create command handler instances
    HomeCommand homeCmd = new HomeCommand(this);
    WarpCommand warpCmd = new WarpCommand(this);
    TeleportCommand tpCmd = new TeleportCommand(this);
    JailCommand jailCmd = new JailCommand(this);
    MiscCommand misc = new MiscCommand(this);
    moderation = new ModerationCommand(this);
    SpeedCommand speedCmd = new SpeedCommand(this);
    ToolCommands tool = new ToolCommands(this);
    PowerToolListener powertoolListener = new PowerToolListener(this);

    // Register commands safely (guard null in case plugin.yml missing entries)
    tryRegister("sethome", homeCmd);
    tryRegister("home", homeCmd);
    tryRegister("delhome", homeCmd);

    tryRegister("setwarp", warpCmd);
    tryRegister("warp", warpCmd);
    tryRegister("delwarp", warpCmd);

    tryRegister("tpa", tpCmd);
    tryRegister("tpaccept", tpCmd);
    tryRegister("tpdeny", tpCmd);
    tryRegister("tphere", tpCmd);

    tryRegister("setjail", jailCmd);
    tryRegister("jail", jailCmd);
    tryRegister("unjail", jailCmd);

    tryRegister("back", misc);
    tryRegister("spawn", misc);
    tryRegister("heal", misc);
    tryRegister("feed", misc);

    tryRegister("mute", moderation);
    tryRegister("unmute", moderation);
    tryRegister("socialspy", moderation);

    tryRegister("speed", speedCmd);

    tryRegister("powertool", tool);
    tryRegister("repair", tool);
    tryRegister("nuke", tool);
    tryRegister("near", tool);
    tryRegister("lightning", tool);
    tryRegister("hat", tool);
    tryRegister("fireball", tool);
    tryRegister("burn", tool);
    tryRegister("ext", tool);
    tryRegister("antioch", tool);
    tryRegister("shout", tool);

    // Register listeners
    getServer().getPluginManager().registerEvents(powertoolListener, this);
    getServer().getPluginManager().registerEvents(tpCmd, this); // TeleportCommand implements Listener for cleanup

    getLogger().info("EssentialsLite enabled.");
  }

  @Override
  public void onDisable() {
    // Save store on shutdown
    if (store != null) store.save();
    getLogger().info("EssentialsLite disabled.");
  }

  private void tryRegister(String name, org.bukkit.command.CommandExecutor executor) {
    if (getCommand(name) != null) {
      getCommand(name).setExecutor(executor);
    } else {
      getLogger().warning("Command '" + name + "' is not defined in plugin.yml and was not registered.");
    }
  }

  public YamlStore getStore() { return store; }
  public ModerationCommand getModeration() { return moderation; }
}
