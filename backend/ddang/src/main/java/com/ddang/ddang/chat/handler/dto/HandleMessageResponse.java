package com.ddang.ddang.chat.handler.dto;

import java.util.List;

public record HandleMessageResponse(SendMessageStatus sendMessageStatus, List<MessageDto> messages) {
}
