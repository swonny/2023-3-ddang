package com.ddang.ddang.websocket.handler.dto;

import java.util.Arrays;

public enum ChattingType {

    MESSAGE("message"),
    PING("ping"),
    ;

    private String value;

    ChattingType(final String value) {
        this.value = value;
    }

    public static ChattingType findValue(final String value) {
        return Arrays.stream(ChattingType.values())
                .filter(chattingType -> chattingType.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 채팅 타입입니다."));
    }
}
