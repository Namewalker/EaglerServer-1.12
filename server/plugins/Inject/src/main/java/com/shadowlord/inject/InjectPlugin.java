package com.shadowlord.inject;

import com.shadowlord.inject.behaviors.BehaviorRegistry;
import com.shadowlord.inject.listeners.InjectListeners;
import org.bukkit.plugin.java.JavaPlugin;

public class InjectPlugin extends JavaPlugin {
  private BehaviorRegistry registry;

  @Override
  public void onEnable() {
    this.registry = new BehaviorRegistry(this);
    // register example behaviors
    registry.register("explodeOnDeath", new com.shadowlord.inject.behaviors.ExplodeOnDeathBehavior());
    registry.register("followPlayer", new com.shadowlord.inject.behaviors.FollowPlayerBehavior(this));

    getCommand("inject").setExecutor(new com.shadowlord.inject.commands.InjectCommand(this, registry));
    getServer().getPluginManager().registerEvents(new InjectListeners(this, registry), this);
    getLogger().info("Inject enabled");
  }

  @Override
  public void onDisable() {
    registry.disableAll();
    getLogger().info("Inject disabled");
  }

  public BehaviorRegistry getRegistry() {
    return registry;
  }
}
