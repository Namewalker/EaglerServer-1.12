package com.thecode.miniedit;

import com.thecode.miniedit.commands.CopyCommand;
import com.thecode.miniedit.commands.SetCommand;
import com.thecode.miniedit.commands.ToolCommand;
import com.thecode.miniedit.commands.WandCommand;
import com.thecode.miniedit.edit.BrushTool;
import com.thecode.miniedit.edit.ClipboardManager;
import com.thecode.miniedit.edit.SelectionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniEditPlugin extends JavaPlugin {
    private SelectionManager selectionManager;
    private ClipboardManager clipboardManager;
    private BrushTool brushTool;

    @Override
    public void onEnable() {
        selectionManager = new SelectionManager(this);
        clipboardManager = new ClipboardManager();
        brushTool = new BrushTool(this);

        getCommand("wand").setExecutor(new WandCommand(selectionManager));
        getCommand("set").setExecutor(new SetCommand(selectionManager));
        getCommand("copy").setExecutor(new CopyCommand(selectionManager, clipboardManager));
        getCommand("tool").setExecutor(new ToolCommand(brushTool));

        getLogger().info("MiniEdit loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MiniEdit disabled!");
    }
}
