package com.dangdangsalon.domain.chat.util;

import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ChatRedisConfig {
    private static final String SAVE_MESSAGE_ROOM_ID_KEY = "chat:messages:";
    private static final String LAST_READ_KEY = "lastRead:";
    private static final String FIRST_LOADED_KEY = "firstLoadedIndex:";
    public static final int MESSAGE_GET_LIMIT = 7;
    public static final Duration TTL = Duration.ofDays(1);

    public String getSaveMessageKey(Long roomId) {
        return SAVE_MESSAGE_ROOM_ID_KEY + roomId;
    }

    public String getLastReadKey(Long roomId, Long userId) {
        return LAST_READ_KEY + roomId + ":" + userId;
    }

    public String getFirstLoadedKey(Long roomId, Long userId) {
        return FIRST_LOADED_KEY + roomId + ":" + userId;
    }

    public String getRoomLastReadKey(Long roomId) {
        return LAST_READ_KEY + roomId + ":*";
    }

    public String getRoomFirstLoadedKey(Long roomId) {
        return FIRST_LOADED_KEY + roomId + ":*";
    }
}
