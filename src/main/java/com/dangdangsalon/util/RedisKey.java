package com.dangdangsalon.util;

public enum RedisKey {

    SAVE_MESSAGE_ROOM_ID_KEY("chat:messages:"),
    LAST_READ_KEY("lastRead:"),
    FIRST_LOADED_KEY("firstLoadedIndex:");

    private final String key;

    RedisKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
