package com.civilization.civil_utils_lib.discord;

import java.util.concurrent.Callable;

import com.civilization.civil_utils_lib.discord.entity.ServerStatusEntity;

public class DiscordEvent {
    @FunctionalInterface
    public interface LongCallable { void method(long value); }
    public final Callable<ServerStatusEntity> onBuildOnlineServerStatusData;
    public final LongCallable onChangedServerStatusMessageID;

    public DiscordEvent(Callable<ServerStatusEntity> onBuildOnlineServerStatusData, LongCallable onServerStatusMessageIDChanged) {
        this.onBuildOnlineServerStatusData = onBuildOnlineServerStatusData;
        this.onChangedServerStatusMessageID = onServerStatusMessageIDChanged;
    }
}
