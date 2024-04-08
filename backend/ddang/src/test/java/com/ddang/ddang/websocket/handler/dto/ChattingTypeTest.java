package com.ddang.ddang.websocket.handler.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChattingTypeTest {

    @Test
    void 타입에_해당하는_enum을_반환한다() {
        // given
        final Map<String, String> data = Map.of("type", "message");

        // when
        final ChattingType actual = ChattingType.findValue(data.get("type"));

        // then
        assertThat(actual).isEqualTo(ChattingType.MESSAGE);
    }


    @Test
    void 해당하는_타입이_없는_경우_예외를_던진다() {
        // given
        final Map<String, String> data = Map.of("type", "wrong type");

        // when & then
        assertThatThrownBy(() -> ChattingType.findValue(data.get("type"))).isInstanceOf(IllegalArgumentException.class);
    }
}
