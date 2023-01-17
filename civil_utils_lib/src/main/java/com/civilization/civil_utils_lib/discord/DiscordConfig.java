package com.civilization.civil_utils_lib.discord;

public class DiscordConfig {
    public final String token;

    public final Long serverStatusChannelID;
    public final Long serverStatusMessageID;

    public final Integer serverStatusRestartHour;
    public final Integer serverStatusRestartMinutes;

    public final int serverStatusPeriod;

    public final String serverStatusTitle;
    public final String serverStatusDescription;
    public final String serverStatusIPAddress;
    public final byte[] serverStatusServerIcon;
    public final String serverStatusServerIconName;
    public final byte[] serverStatusIPIcon;
    public final String serverStatusIPIconName;
    public final byte[] serverStatusRestartIcon;
    public final String serverStatusRestartIconName;
    public final String serverStatusModpackVersion;

    public DiscordConfig(String token, Long serverStatusChannelID, Long serverStatusMessageID,
            Integer serverStatusRestartHour, Integer serverStatusRestartMinutes, int serverStatusPeriod,
            String serverStatusTitle, String serverStatusDescription, String serverStatusIPAddress,
            byte[] serverStatusServerIcon, String serverStatusServerIconName, byte[] serverStatusIPIcon,
            String serverStatusIPIconName, byte[] serverStatusRestartIcon, String serverStatusRestartIconName,
            String serverStatusModpackVersion) {
        this.token = token;
        this.serverStatusChannelID = serverStatusChannelID;
        this.serverStatusMessageID = serverStatusMessageID;
        this.serverStatusRestartHour = serverStatusRestartHour;
        this.serverStatusRestartMinutes = serverStatusRestartMinutes;
        this.serverStatusPeriod = serverStatusPeriod;
        this.serverStatusTitle = serverStatusTitle;
        this.serverStatusDescription = serverStatusDescription;
        this.serverStatusIPAddress = serverStatusIPAddress;
        this.serverStatusServerIcon = serverStatusServerIcon;
        this.serverStatusServerIconName = serverStatusServerIconName;
        this.serverStatusIPIcon = serverStatusIPIcon;
        this.serverStatusIPIconName = serverStatusIPIconName;
        this.serverStatusRestartIcon = serverStatusRestartIcon;
        this.serverStatusRestartIconName = serverStatusRestartIconName;
        this.serverStatusModpackVersion = serverStatusModpackVersion;
    }

}
