package com.civilization.civil_utils_lib.discord;

import com.civilization.civil_utils_lib.discord.api.PlayersChart;
import com.civilization.civil_utils_lib.discord.entity.ServerStatusEntity;
import com.civilization.civil_utils_lib.discord.utils.Variables;
import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import static java.lang.Thread.sleep;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Discord {
    private DiscordConfig config;
    private DiscordEvent events;

    private JDA jda = null;

    private Timer serverStatusTimer;
    private Long serverStatusMessageID;
    private Instant serverStatusRestartTimestamp;

    private boolean isRunServerStatusTimer = false;

    private ArrayList<Integer> serverStatusRecentlyPlayers;

    private Date startTime;
    private Date serverStartTime;

    private void init(DiscordConfig config) {
        while (true) {
            if (config.token != null && !config.token.isEmpty()) {
                final JDABuilder builder = JDABuilder.createDefault(config.token, GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));

                builder.setAutoReconnect(true);
                builder.setStatus(OnlineStatus.ONLINE);
                builder.setActivity(Activity.playing("Сервер 'Цивилизация'"));

                try {
                    jda = builder.build();
                    jda.awaitReady();
                    break;
                } catch (InvalidTokenException e) {
                    if (e.getMessage().equals("The provided token is invalid!")) {
                        Variables.LOGGER.error("Invalid token, please set correct token in the config file!");
                        return;
                    }
                    Variables.LOGGER.error("Login failed, retrying");
                    try {
                        // noinspection BusyWait
                        sleep(6000);
                    } catch (InterruptedException ignored) {
                        return;
                    }
                } catch (InterruptedException | IllegalStateException e) {
                    return;
                }
            } else {
                Variables.LOGGER.error(
                        "An incorrectly entered token or token is not specified. Enter the token in the configuration file");
                return;
            }
        }
    }

    private static class BuildResult {
        public MessageEmbed embed;
        public ArrayList<FileUpload> files;

        public BuildResult(MessageEmbed embed, ArrayList<FileUpload> files) {
            this.embed = embed;
            this.files = files;
        }
    }

    private static class WorldTime {
        public int hour;
        public int minutes;

        public WorldTime(int hour, int minutes) {
            this.hour = hour;
            this.minutes = minutes;
        }
    }

    private WorldTime getWorldTime(long time) {
        int cutTime = (int) ((time + 6000) % 24000);
        int hour = cutTime / 1000;
        int minutes = (int) Math.floor((double) cutTime / 16.6666666) % 60;

        return new WorldTime(hour, minutes);
    }

    private String worldTimeToString(WorldTime time) {
        String correctHour = time.hour < 10 ? "0" + time.hour : String.valueOf(time.hour);
        String correctMinutes = time.minutes < 10 ? "0" + time.minutes : String.valueOf(time.minutes);

        return correctHour + ":" + correctMinutes;
    }

    private BuildResult buildOfflineServerStatusMessage() {
        // Строим окошечко
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (config.serverStatusTitle != null && !config.serverStatusTitle.isEmpty())
            embedBuilder.setTitle(config.serverStatusTitle);

        if (config.serverStatusDescription != null && !config.serverStatusDescription.isEmpty())
            embedBuilder.setDescription(config.serverStatusDescription);

        embedBuilder.setThumbnail("attachment://" + config.serverStatusServerIconName);

        if (config.serverStatusIPAddress != null && !config.serverStatusIPAddress.isEmpty())
            embedBuilder.setAuthor(config.serverStatusIPAddress, null, "attachment://" + config.serverStatusIPIconName);
        embedBuilder.addField("Статус", "Offline", true);

        if (config.serverStatusModpackVersion != null && !config.serverStatusModpackVersion.isEmpty())
            embedBuilder.addField("Версия сборки", config.serverStatusModpackVersion, true);

        embedBuilder.setColor(0xFF0000);

        // Добавляем все используемые файлы к сообщению
        ArrayList<FileUpload> files = new ArrayList<FileUpload>();

        if (config.serverStatusIPIcon != null && config.serverStatusIPIconName != null) {
            files.add(FileUpload.fromData(config.serverStatusIPIcon,
                    config.serverStatusIPIconName));
        }
        if (config.serverStatusServerIcon != null && config.serverStatusServerIconName != null) {
            files.add(FileUpload.fromData(config.serverStatusServerIcon,
                    config.serverStatusServerIconName));
        }

        return new BuildResult(embedBuilder.build(), files);
    }

    private static final String[] seasonArray = {
            ":hibiscus: Весна",
            ":four_leaf_clover: Лето",
            ":fallen_leaf: Осень",
			":snowflake: Зима"
    };

    private static final String[] weatherArray = {
            ":sunny: Солнечно",
            ":cloud_rain: Дождь",
            ":thunder_cloud_rain: Гроза",
            ":cloud_snow: Снег"
    };

    private static final String[] timeEmojiArray = {
            ":new_moon:",
            ":waxing_crescent_moon:",
            ":first_quarter_moon:",
            ":waxing_gibbous_moon:",
            ":full_moon:",
            ":waning_gibbous_moon:",
            ":last_quarter_moon:",
            ":waning_crescent_moon:",
    };

    private BuildResult buildOnlineServerStatusMessage(ServerStatusEntity entity) {
        // Строим окошечко
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (config.serverStatusTitle != null && !config.serverStatusTitle.isEmpty())
            embedBuilder.setTitle(config.serverStatusTitle);

        if (config.serverStatusDescription != null && !config.serverStatusDescription.isEmpty())
            embedBuilder.setDescription(config.serverStatusDescription);

        embedBuilder.setThumbnail("attachment://" + config.serverStatusServerIconName);

        if (config.serverStatusIPAddress != null && !config.serverStatusIPAddress.isEmpty())
            embedBuilder.setAuthor(config.serverStatusIPAddress, null, "attachment://" + config.serverStatusIPIconName);
        embedBuilder.addField("Статус", "Online", true);

        if (config.serverStatusModpackVersion != null && !config.serverStatusModpackVersion.isEmpty())
            embedBuilder.addField("Версия сборки", config.serverStatusModpackVersion, true);

        embedBuilder.setColor(0x00FF00);

        embedBuilder.setFooter("Перезапуск сервера произойдет", "attachment://" + config.serverStatusRestartIconName)
                .setTimestamp(serverStatusRestartTimestamp);

        String countPlayer = entity.countPlayer + "/" + entity.maxPlayer;
        String players = "";
        for (String player : entity.players) {
            players += "`" + player + "` ";
        }

        String seasons = seasonArray[entity.seasons.ordinal()];
        String weather = weatherArray[entity.weather.ordinal()];

        WorldTime time = getWorldTime(entity.time);
        String timeEmoji = timeEmojiArray[((time.hour + 1) % 24) / 3];

        embedBuilder.addField("Кол-во игроков", countPlayer, true)
                .addField("Игроки", players, false)
                .addField("Время года", seasons, true)
                .addField("Погода", weather, true)
                .addField("Время", timeEmoji + " " + worldTimeToString(time), true);

        embedBuilder.setImage("attachment://players-chart.png");

        // Добавляем все используемые файлы к сообщению
        ArrayList<FileUpload> files = new ArrayList<FileUpload>();

        if (config.serverStatusIPIcon != null && config.serverStatusIPIconName != null) {
            files.add(FileUpload.fromData(config.serverStatusIPIcon,
                    config.serverStatusIPIconName));
        }
        if (config.serverStatusServerIcon != null && config.serverStatusServerIconName != null) {
            files.add(FileUpload.fromData(config.serverStatusServerIcon,
                    config.serverStatusServerIconName));
        }
        if (config.serverStatusRestartIcon != null && config.serverStatusRestartIconName != null) {
            files.add(FileUpload.fromData(config.serverStatusRestartIcon,
                    config.serverStatusRestartIconName));
        }

        serverStatusRecentlyPlayers.add(entity.countPlayer);

        byte[] playersChart = null;

        try {
            playersChart = PlayersChart.build(serverStatusRecentlyPlayers, serverStartTime, config.serverStatusPeriod);
        } catch (IOException e) {
            Variables.LOGGER.error("Error creating a player chart recently");
        }

        if (playersChart != null) {
            files.add(FileUpload.fromData(playersChart, "players-chart.png"));
        }

        return new BuildResult(embedBuilder.build(), files);
    }

    private void sendServerStatusMessage(BuildResult buildResult, boolean isComplete) {
        if (config.serverStatusChannelID != null) {
            TextChannel channel = jda.getTextChannelById(config.serverStatusChannelID);

            if (channel == null) {
                Variables.LOGGER.error(
                        "An incorrectly entered channelID or channelID is not specified. Enter the channelID in the configuration file");
                return;
            }

            if (serverStatusMessageID != null) {
                // Изменяем имеющиеся сообщение
                RestAction<Message> actionMes = channel.retrieveMessageById(serverStatusMessageID);

                try {
                    if (isComplete) {
                        try {
                            Message mes = actionMes.complete();

                            mes.editMessageEmbeds(buildResult.embed)
                                    .setAttachments()
                                    .setFiles(buildResult.files)
                                    .queue();
                        } catch (Exception e) {
                            Variables.LOGGER.error("AAAAAAAAAAA");
                            return;
                        }
                    } else {
                        actionMes.queue(
                                t -> t.editMessageEmbeds(buildResult.embed)
                                        .setAttachments()
                                        .setFiles(buildResult.files)
                                        .queue(),
                                t -> {
                                    Variables.LOGGER
                                            .error("The message displaying server information could not be changed");
                                });
                    }
                } catch (Exception e) {
                    Variables.LOGGER.error("Incorrectly entered messageID. Enter the correct messageID");
                    return;
                }
            } else {
                // Отправляем новое сообщение
                MessageCreateAction actionMes = channel.sendMessageEmbeds(buildResult.embed)
                        .addFiles(buildResult.files);

                if (isComplete) {
                    try {
                        Message mes = actionMes.complete();

                        serverStatusMessageID = mes.getIdLong();
                        if (events.onChangedServerStatusMessageID != null) {
                            events.onChangedServerStatusMessageID.method(serverStatusMessageID);
                        }
                    } catch (Exception e) {
                        Variables.LOGGER.error("AAAAAAAAAAA");
                        return;
                    }
                } else {
                    actionMes.queue(t -> {
                        serverStatusMessageID = t.getIdLong();
                        if (events.onChangedServerStatusMessageID != null) {
                            events.onChangedServerStatusMessageID.method(serverStatusMessageID);
                        }
                    });
                }
            }
        } else {
            Variables.LOGGER.error(
                    "An incorrectly entered channelID or channelID is not specified. Enter the channelID in the configuration file");
        }
    }

    // Задачи бота

    private void runServerStatus() {
        if (events.onBuildOnlineServerStatusData != null) {
            Variables.LOGGER.info("Starting the task of displaying server information");

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        sendServerStatusMessage(
                                buildOnlineServerStatusMessage(events.onBuildOnlineServerStatusData.call()), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            int period = config.serverStatusPeriod;
            long minuteToSecondsPeriod = (long) period * 1000L * 60L;

            try {
                serverStatusTimer = new Timer("Civil_Discord_Server_Status");
                serverStatusTimer.scheduleAtFixedRate(timerTask, 0L, minuteToSecondsPeriod);

                isRunServerStatusTimer = true;
            } catch (Exception e) {
                Variables.LOGGER.error("Timer start error: " + e.getMessage());
            }
        }
    }

    // Этапы отключения бота

    private void shutdownServerStatus() {
        Variables.LOGGER.info("Disabling the task of displaying server information");
        try {
            if (isRunServerStatusTimer) {
                serverStatusTimer.cancel();

                isRunServerStatusTimer = false;
            }

            sendServerStatusMessage(buildOfflineServerStatusMessage(),
                    true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Манипуляции с ботом

    public void setup(@NotNull DiscordConfig config, @NotNull DiscordEvent events) {
        this.config = config;
        this.events = events;

        startTime = new Date();

        serverStatusMessageID = config.serverStatusMessageID;
        serverStatusRecentlyPlayers = new ArrayList<Integer>();

        if (config.serverStatusRestartHour != null && config.serverStatusRestartMinutes != null) {
            serverStatusRestartTimestamp = new Date(new Date().getTime() +
                    (config.serverStatusRestartHour * 60L * 60L * 1000L) +
                    (config.serverStatusRestartMinutes * 60L * 1000L)).toInstant();
        } else {
            serverStatusRestartTimestamp = null;
        }

        Variables.LOGGER.info("Starting the initial setup of the Discord Bot");
        init(config);
    }

    public void shutdown() {
        if (jda != null) {
            Variables.LOGGER.info("Disabling the Discord Bot");

            shutdownServerStatus();

            jda.shutdown();
            Variables.LOGGER.info("Discord Bot is disabled");
        }
    }

    public void run() {
        if (jda != null) {
            serverStartTime = new Date();

            Variables.LOGGER.info("Run all Discord Bot tasks");
            runServerStatus();
        }
    }
}
