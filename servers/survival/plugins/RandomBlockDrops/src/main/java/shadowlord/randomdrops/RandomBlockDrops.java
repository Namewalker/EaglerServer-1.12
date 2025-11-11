package shadowlord.randomdrops;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomBlockDrops extends JavaPlugin {
    private BlockDropManager dropManager;
    private DropConfigGUI gui;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.dropManager = new BlockDropManager(this);
        this.gui = new DropConfigGUI(this);

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);

        getLogger().info("RandomBlockDrops enabled");
    }

    @Override
    public void onDisable() {
        dropManager.saveMappings();
        getLogger().info("RandomBlockDrops disabled");
    }

    public BlockDropManager getDropManager() {
        return dropManager;
    }

    public DropConfigGUI getGui() {
        return gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("randomdrops")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be run in-game.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("randomdrops.use")) {
            player.sendMessage("You don't have permission to use this.");
            return true;
        }

        gui.openMainGui(player, 0);
        return true;
    }
}
