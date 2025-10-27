package com.shadowlord.inject;

import com.shadowlord.inject.behaviors.*;
import com.shadowlord.inject.commands.InjectCommand;
import com.shadowlord.inject.commands.PatrolCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class InjectPlugin extends JavaPlugin {
  private BehaviorRegistry registry;

  @Override
  public void onEnable() {
    // Initialize behavior registry
    this.registry = new BehaviorRegistry(this);

    // Register available behaviors
    registry.register("explodeOnDeath", new ExplodeOnDeathBehavior());
    registry.register("followPlayer", new FollowPlayerBehavior(this));
    registry.register("summonMinions", new SummonMinionsBehavior(this));
    registry.register("healNearbyAllies", new HealNearbyAlliesBehavior());
    registry.register("patrolPoints", new PatrolPointsBehavior(this));

    // Register commands
    if (getCommand("inject") != null) {
      getCommand("inject").setExecutor(new InjectCommand(this, registry));
      getLogger().info("Registered command: /inject");
    } else {
      getLogger().warning("Command 'inject' not found in plugin.yml");
    }

    if (getCommand("patrol") != null) {
      getCommand("patrol").setExecutor(new PatrolCommand(this));
      getLogger().info("Registered command: /patrol");
    } else {
      getLogger().warning("Command 'patrol' not found in plugin.yml");
    }

    getLogger().info("Inject plugin enabled.");
  }

  @Override
  public void onDisable() {
    if (registry != null) registry.disableAll();
    getLogger().info("Inject plugin disabled.");
  }

  public BehaviorRegistry getRegistry() {
    return registry;
  }
}
