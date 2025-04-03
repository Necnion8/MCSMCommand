package com.gmail.necnionch.myplugin.mcsmcommand.bukkit.mcsm;

import java.util.UUID;

public class MCSMInstance {

    private final UUID uuid;
    private final String nickname;
    private final int currentPlayers;
    private final int maxPlayers;
    private final String version;
    private final Status status;
    private final int statusValue;

    public MCSMInstance(
            UUID uuid,
            String nickname,
            int currentPlayers,
            int maxPlayers,
            String version,
            Status status,
            int statusValue
    ) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
        this.version = version;
        this.status = status;
        this.statusValue = statusValue;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getNickname() {
        return nickname;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getVersion() {
        return version;
    }

    public Status getStatus() {
        return status;
    }

    public int getStatusValue() {
        return statusValue;
    }

    public enum Status {
        UNKNOWN(-999),
        BUSY(-1),
        STOPPED(0),
        STOPPING(1),
        STARTING(2),
        RUNNING(3);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Status valueOf(int value) {
            switch (value) {
                case -1:
                    return BUSY;
                case 0:
                    return STOPPED;
                case 1:
                    return STOPPING;
                case 2:
                    return STARTING;
                case 3:
                    return RUNNING;
            }
            return UNKNOWN;
        }

    }

}
