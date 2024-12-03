package com.dangdangsalon.domain.chat.util;

public enum ChatConst {
    MESSAGE_GET_LIMIT(7);

    private int count;

    ChatConst(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
