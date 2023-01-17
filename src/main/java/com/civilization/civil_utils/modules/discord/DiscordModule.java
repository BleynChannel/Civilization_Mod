package com.civilization.civil_utils.modules.discord;

import com.civilization.civil_utils.Config;
import com.civilization.civil_utils_lib.discord.Discord;
import com.civilization.civil_utils_lib.discord.DiscordConfig;
import com.civilization.civil_utils_lib.discord.DiscordEvent;
import com.civilization.civil_utils_lib.discord.entity.ServerStatusEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

public class DiscordModule {
    private static final Logger LOGGER = LogManager.getLogger(DiscordModule.class);

    private Discord discord = null;

    private MinecraftServer server = null;

    //------------------------------------//

    private ServerStatusEntity buildOfflineServerStatusData() {
        LOGGER.info("Build Offline message");
        return ServerStatusEntity.offline();
    }

    private ServerStatusEntity buildOnlineServerStatusData() {
        LOGGER.info("Build Online message");

        PlayerList playerList = server.getPlayerList();

        int countPlayer = playerList.getPlayerCount();
        int maxPlayer = playerList.getMaxPlayers();

        ArrayList<String> players = new ArrayList<String>();

        for (ServerPlayer player : playerList.getPlayers()) {
            players.add(player.getDisplayName().getString());
        }

        ServerStatusEntity.Seasons seasons = ServerStatusEntity.Seasons.SPRING;
        ServerStatusEntity.Weather weather = ServerStatusEntity.Weather.SUNNY;
        long time = -1;

        Iterable<ServerLevel> worlds = server.getAllLevels();
		for (ServerLevel world : worlds) {
			ResourceLocation rl = world.dimension().location();
			String worldId = rl.getNamespace() + "_" + rl.getPath();

			if (worldId.equals("minecraft_overworld")) {
                Season season = SeasonHelper.getSeasonState(world).getSeason();
                seasons = ServerStatusEntity.Seasons.values()[season.ordinal()];

                if (seasons.equals(ServerStatusEntity.Seasons.WINTER) && world.isRaining()) {
                    weather = ServerStatusEntity.Weather.SNOW;
                } else if (world.isThundering()) {
                    weather = ServerStatusEntity.Weather.THUNDERSTORM;
                } else if (world.isRaining()) {
                    weather = ServerStatusEntity.Weather.RAIN;
                } else if (!world.isRaining() && !world.isThundering()) {
                    weather = ServerStatusEntity.Weather.SUNNY;
                }

                time = world.getDayTime();
            }
		}

        return ServerStatusEntity.online(
                countPlayer,
                maxPlayer,
                players,
                seasons,
                weather,
                time);
    }

    private void changeServerStatusMessageID(Long messageID) {
        Config.DISCORD_SERVER_STATUS_MESSAGE_ID.set(String.valueOf(messageID));
        Config.DISCORD_SERVER_STATUS_MESSAGE_ID.save();
        LOGGER.info("Saving message ID: " + messageID.toString());
    }

    private DiscordConfig buildConfig() {
        ClassLoader loader = getClass().getClassLoader();

        byte[] serverIcon = null;
        byte[] ipIcon = null;
        byte[] restartIcon = null;

        try {
            var serverIconBuf = new BufferedInputStream(Objects.requireNonNull(loader.getResourceAsStream("assets/discord/server-icon.png")));
            var ipIconBuf = new BufferedInputStream(Objects.requireNonNull(loader.getResourceAsStream("assets/discord/ip.png")));
            var restartIconBuf = new BufferedInputStream(Objects.requireNonNull(loader.getResourceAsStream("assets/discord/restart.png")));

            serverIcon = serverIconBuf.readAllBytes();
            ipIcon = ipIconBuf.readAllBytes();
            restartIcon = restartIconBuf.readAllBytes();
        } catch (IOException e) {
            LOGGER.error("File error: " + e.getMessage());
            e.printStackTrace();
        }

        String channelIDString = Config.DISCORD_SERVER_STATUS_CHANNEL_ID.get();
        Long channelID = null;

        try {
            channelID = Long.parseLong(channelIDString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        String messageIDString = Config.DISCORD_SERVER_STATUS_MESSAGE_ID.get();
        Long messageID = null;

        try {
            messageID = Long.parseLong(messageIDString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        int period = Config.DISCORD_SERVER_STATUS_PERIOD.get();
        String periodDescription = "";

        if (period == 1) {
            periodDescription = "каждую минуту";
        } else if (period > 1 && period < 5) {
            periodDescription = "каждые " + String.valueOf(period) + " минуты";
        } else if (period >= 5 && period < 60) {
            periodDescription = "каждые " + String.valueOf(period) + " минут";
        } else {
            periodDescription = "каждый час";
        }

        return new DiscordConfig(
                Config.DISCORD_TOKEN.get(),
                channelID,
                messageID,
                Config.DISCORD_SERVER_STATUS_RESTART_HOUR.get(),
                Config.DISCORD_SERVER_STATUS_RESTART_MINUTES.get(),
                period,
                Config.DISCORD_SERVER_STATUS_TITLE.get(),
                "Информация обновляется " + periodDescription,
                Config.DISCORD_SERVER_STATUS_IP_ADDRESS.get(),
                serverIcon,
                "server-icon.png",
                ipIcon,
                "ip.png",
                restartIcon,
                "restart.png",
                Config.DISCORD_SERVER_STATUS_MODPACK_VERSION.get());
    }

    private DiscordEvent buildEvents() {
        return new DiscordEvent(this::buildOnlineServerStatusData, this::changeServerStatusMessageID);
    }

    //------------------------------------//

    public void setup(final FMLCommonSetupEvent ev) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            LOGGER.info("Starting Discord module on server");

            discord = new Discord();

            try {
                LOGGER.info("Initializing Discord Bot...");
                discord.setup(buildConfig(), buildEvents());
                LOGGER.info("Initializing Successful!");
            } catch (Exception e) {
                e.printStackTrace();
                discord = null;
                return;
            }
        } else {
            LOGGER.info("Starting Discord module on client");
            discord = null;
        }
    }

    public void serverStarting(ServerStartingEvent ev) {
        server = ev.getServer();

        if (discord != null) {
            LOGGER.info("Discord Bot is started");
            discord.run();
            LOGGER.info("Discord Bot is staring");
        }
    }

    public void serverStopping(ServerStoppingEvent ev) {
        if (discord != null) {
            LOGGER.info("Discord Bot is stoped");
            discord.shutdown();
            LOGGER.info("Discord Bot is stoping");
        }
    }
}
