package com.dangdangsalon.util;

public enum RedisKey {

    SAVE_MESSAGE_ROOM_ID_KEY("chat:messages:"),
    LAST_READ_KEY("lastRead:"),
    FIRST_LOADED_KEY("firstLoadedIndex:");

    private final String keyString;

    RedisKey(String keyString) {
        this.keyString = keyString;
    }
}
