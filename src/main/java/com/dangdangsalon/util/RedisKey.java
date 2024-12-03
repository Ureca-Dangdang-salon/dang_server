package com.dangdangsalon.util;

public enum RedisKey {

    SAVE_MESSAGE_ROOM_ID_KEY("chat:messages:roomId="),
    LAST_READ_KEY("lastReadSequence:"),
    FIRST_LOADED_KEY("firstLoadedSequence:"),
    SEQUENCE_KEY("chat:sequence:roomId=");

    private final String key;

    RedisKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
