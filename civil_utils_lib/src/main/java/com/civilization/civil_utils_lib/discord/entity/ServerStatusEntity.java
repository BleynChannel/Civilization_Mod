package com.civilization.civil_utils_lib.discord.entity;

import java.util.ArrayList;

public class ServerStatusEntity {
    public enum Seasons {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER,
    }

    public enum Weather {
        SUNNY,
        RAIN,
        THUNDERSTORM,
        SNOW,
    }

    public final Integer countPlayer;
    public final Integer maxPlayer;
    public final ArrayList<String> players;

    public final Seasons seasons;
    public final Weather weather;
    public final Long time;

    private ServerStatusEntity(Integer countPlayer, Integer maxPlayer, ArrayList<String> players, Seasons seasons,
            Weather weather, Long time) {
        this.countPlayer = countPlayer;
        this.maxPlayer = maxPlayer;
        this.players = players;
        this.seasons = seasons;
        this.weather = weather;
        this.time = time;
    }

    public static ServerStatusEntity offline() {
        return new ServerStatusEntity(null, null, null, null, null, null);
    }

    public static ServerStatusEntity online(int countPlayer, int maxPlayer, ArrayList<String> players,
            Seasons seasons, Weather weather, long time) {
        return new ServerStatusEntity(countPlayer, maxPlayer, players, seasons, weather, time);
    }
}
