package com.gmail.necnionch.myplugin.mcsmcommand.bukkit.config;

import org.bukkit.plugin.Plugin;

public class MainConfig extends BukkitConfiguration{
    public MainConfig(Plugin plugin) {
        super(plugin);
    }

    public String getAPIUrl() {
        return config.getString("api-url", "http://localhost:23333");
    }

    public String getAPIKey() {
        return config.getString("api-key");
    }

    public String getDaemonId() {
        return config.getString("daemon-id");
    }

}
