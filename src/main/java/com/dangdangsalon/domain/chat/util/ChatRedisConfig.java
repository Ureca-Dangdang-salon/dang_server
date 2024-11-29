package com.dangdangsalon.domain.chat.util;

import static com.dangdangsalon.util.RedisKey.*;

import com.dangdangsalon.util.RedisKey;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ChatRedisConfig {
    public static final Duration TTL = Duration.ofDays(1);

    public String getSaveMessageKey(Long roomId) {
        return SAVE_MESSAGE_ROOM_ID_KEY.name() + roomId;
    }

    public String getLastReadKey(Long roomId, Long userId) {
        return LAST_READ_KEY.name() + roomId + ":" + userId;
    }

    public String getFirstLoadedKey(Long roomId, Long userId) {
        return FIRST_LOADED_KEY.name() + roomId + ":" + userId;
    }

    public String getRoomLastReadKey(Long roomId) {
        return LAST_READ_KEY.name() + roomId + ":*";
    }

    public String getRoomFirstLoadedKey(Long roomId) {
        return FIRST_LOADED_KEY.name() + roomId + ":*";
    }
}
