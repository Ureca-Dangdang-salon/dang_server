package com.dangdangsalon.domain.chat.util;

import static com.dangdangsalon.util.RedisKey.*;

import com.dangdangsalon.util.RedisKey;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ChatRedisConfig {
    public static final Duration TTL = Duration.ofDays(1);

    public String getSaveMessageKey(Long roomId) {
        return RedisKey.SAVE_MESSAGE_ROOM_ID_KEY.getKey() + roomId;
    }

    public String getAllSaveMessageKey() {
        return RedisKey.SAVE_MESSAGE_ROOM_ID_KEY.getKey() + "*";
    }

    public String getLastReadKey(Long roomId, Long userId) {
        return RedisKey.LAST_READ_KEY.getKey() + "roomId=" + roomId + ":userId=" + userId;
    }

    public String getFirstLoadedKey(Long roomId, Long userId) {
        return RedisKey.FIRST_LOADED_KEY.getKey() + "roomId=" + roomId + ":userId=" + userId;
    }

    public String getSequenceKey(Long roomId) {
        return RedisKey.SEQUENCE_KEY.getKey() + roomId;
    }
}
