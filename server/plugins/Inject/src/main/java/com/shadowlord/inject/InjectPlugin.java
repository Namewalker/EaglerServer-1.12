package com.shadowlord.inject;

import com.shadowlord.inject.behaviors.BehaviorRegistry;
import com.shadowlord.inject.commands.InjectCommand;
import com.shadowlord.inject.commands.PatrolCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class InjectPlugin extends JavaPlugin {
  private BehaviorRegistry registry;

  @Override
  public void onEnable() {
    this.registry = new BehaviorRegistry(this);

    // Register example behaviors here if you have implementations:
    // registry.register("explodeOnDeath", new com.shadowlord.inject.behaviors.ExplodeOnDeathBehavior());
    // registry.register("followPlayer", new com.shadowlord.inject.behaviors.FollowPlayerBehavior(this));

    // Register commands with null checks and logging so missing plugin.yml entries are obvious
    if (getCommand("inject") != null) {
      getCommand("inject").setExecutor(new InjectCommand(this, registry));
      getLogger().info("Registered command: inject");
    } else {
      getLogger().warning("Command 'inject' not found in plugin.yml");
    }

    if (getCommand("patrol") != null) {
      getCommand("patrol").setExecutor(new PatrolCommand(this));
      getLogger().info("Registered command: patrol");
    } else {
      getLogger().warning("Command 'patrol' not found in plugin.yml");
    }

    getLogger().info("Inject enabled");
  }

  @Override
  public void onDisable() {
    if (registry != null) registry.disableAll();
    getLogger().info("Inject disabled");
  }

  public BehaviorRegistry getRegistry() { return registry; }
}
