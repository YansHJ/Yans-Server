package com.example.server.enums;

import lombok.Getter;

public enum EventEnums {
    NEW_CONNECT("newConnect"),

    ONLINE_PLAYERS("onlinePlayers"),

    GET_CLIENT_ID("getClientId"),

    JOIN_SCENE("joinScene"),

    I_MOVED("IMoved"),

    SOMEONE_MOVED("someoneMoved"),

    SOMEONE_LEVELED("someoneLeveled"),

    SOMEONE_LEVEL_ROOM("someoneLevelRoom");

    @Getter
    private final String name;

    EventEnums(String name){
        this.name = name;
    }
}
