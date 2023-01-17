package com.civilization.civil_utils;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    //Discord Module
    public static final ForgeConfigSpec.ConfigValue<String> DISCORD_TOKEN;
    public static final ForgeConfigSpec.ConfigValue<String> DISCORD_SERVER_STATUS_CHANNEL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> DISCORD_SERVER_STATUS_MESSAGE_ID;
    public static final ForgeConfigSpec.ConfigValue<Integer> DISCORD_SERVER_STATUS_RESTART_HOUR;
    public static final ForgeConfigSpec.ConfigValue<Integer> DISCORD_SERVER_STATUS_RESTART_MINUTES;
    public static final ForgeConfigSpec.ConfigValue<Integer> DISCORD_SERVER_STATUS_PERIOD;
    public static final ForgeConfigSpec.ConfigValue<String> DISCORD_SERVER_STATUS_TITLE;
    public static final ForgeConfigSpec.ConfigValue<String> DISCORD_SERVER_STATUS_IP_ADDRESS;
    public static final ForgeConfigSpec.ConfigValue<String> DISCORD_SERVER_STATUS_MODPACK_VERSION;

    //Restart Module
    public static final ForgeConfigSpec.ConfigValue<Integer> RESTART_HOUR;
    public static final ForgeConfigSpec.ConfigValue<Integer> RESTART_MINUTES;
    public static final ForgeConfigSpec.ConfigValue<String> RESTART_MESSAGE;
    public static final ForgeConfigSpec.ConfigValue<String> RESTART_STOP_MESSAGE;

    static {
        BUILDER.push("Discord");

        DISCORD_TOKEN = BUILDER.define("Token", "");

        DISCORD_SERVER_STATUS_CHANNEL_ID = BUILDER.define("[Server Status] Channel ID", "");
        DISCORD_SERVER_STATUS_MESSAGE_ID = BUILDER.define("[Server Status] Message ID", "");
        DISCORD_SERVER_STATUS_RESTART_HOUR = BUILDER.defineInRange("[Server Status] Restart in hour", 8, 0, 10);
        DISCORD_SERVER_STATUS_RESTART_MINUTES = BUILDER.defineInRange("[Server Status] Restart in minutes", 0, 0, 60);
        DISCORD_SERVER_STATUS_PERIOD = BUILDER.defineInRange("[Server Status] Recheck period time", 5, 1, 60);
        DISCORD_SERVER_STATUS_TITLE = BUILDER.define("[Server Status] Title", "");
        DISCORD_SERVER_STATUS_IP_ADDRESS = BUILDER.define("[Server Status] IP Address", "");
        DISCORD_SERVER_STATUS_MODPACK_VERSION = BUILDER.define("[Server Status] Modpack Version", "");

        BUILDER.pop();
        BUILDER.push("Restart");

        RESTART_HOUR = BUILDER.defineInRange("Restart in hour", 8, 1, 10);
        RESTART_MINUTES = BUILDER.defineInRange("Restart in minutes", 0, 0, 60);
        RESTART_MESSAGE = BUILDER.comment("%s - Will insert a time counter").define("Message", "");
        RESTART_STOP_MESSAGE = BUILDER.define("Stop message", "");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
