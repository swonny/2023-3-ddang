package com.ddang.ddang.websocket.handler.dto;

import java.util.Arrays;
import java.util.Map;

public enum ChattingType {

    CHATTING_TYPE_KEY("type"),
    MESSAGE("message"),
    PING("ping"),
    ;

    private String value;

    ChattingType(final String value) {
        this.value = value;
    }

    public static ChattingType findValue(final Map<String, String> data) {
        final String value = data.get(CHATTING_TYPE_KEY.value);
        return Arrays.stream(ChattingType.values())
                .filter(chattingType -> chattingType.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 채팅 타입입니다."));
    }
}
