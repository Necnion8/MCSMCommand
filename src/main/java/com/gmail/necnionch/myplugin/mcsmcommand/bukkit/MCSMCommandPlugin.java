package com.gmail.necnionch.myplugin.mcsmcommand.bukkit;

import com.gmail.necnionch.myplugin.mcsmcommand.bukkit.command.BukkitCommand;
import com.gmail.necnionch.myplugin.mcsmcommand.bukkit.command.MainCommand;
import com.gmail.necnionch.myplugin.mcsmcommand.bukkit.config.MainConfig;
import com.gmail.necnionch.myplugin.mcsmcommand.bukkit.mcsm.MCSMRequestExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCSMCommandPlugin extends JavaPlugin {

    private final BukkitCommand.Compat compat = BukkitCommand.compat(this);
    private final MainConfig mainConfig = new MainConfig(this);
    private MCSMRequestExecutor mcsm;

    @Override
    public void onEnable() {
        compat.init();
        mainConfig.load();
        mcsm = new MCSMRequestExecutor(
                mainConfig.getAPIUrl(), mainConfig.getAPIKey(), mainConfig.getDaemonId(),
                task -> getServer().getScheduler().runTaskAsynchronously(this, task)
        );

        compat.register(new MainCommand(mcsm));
    }

    @Override
    public void onDisable() {
        mcsm = null;
        compat.close();
    }
}
