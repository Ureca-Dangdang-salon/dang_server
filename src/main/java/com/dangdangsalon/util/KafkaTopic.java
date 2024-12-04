package com.dangdangsalon.util;

public enum KafkaTopic {

    CHAT_TOPIC("chat-topic");

    private final String topic;

    KafkaTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return this.topic;
    }
}
