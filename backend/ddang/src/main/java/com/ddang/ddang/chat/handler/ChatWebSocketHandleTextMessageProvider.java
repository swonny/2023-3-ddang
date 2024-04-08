package com.ddang.ddang.chat.handler;

import com.ddang.ddang.chat.application.MessageService;
import com.ddang.ddang.chat.application.dto.CreateMessageDto;
import com.ddang.ddang.chat.application.dto.ReadMessageDto;
import com.ddang.ddang.chat.application.event.MessageNotificationEvent;
import com.ddang.ddang.chat.application.event.UpdateReadMessageLogEvent;
import com.ddang.ddang.chat.domain.Message;
import com.ddang.ddang.chat.domain.WebSocketChatSessions;
import com.ddang.ddang.chat.handler.dto.ChatMessageDataDto;
import com.ddang.ddang.chat.handler.dto.ChatPingDataDto;
import com.ddang.ddang.chat.handler.dto.HandleMessageResponse;
import com.ddang.ddang.chat.handler.dto.MessageDto;
import com.ddang.ddang.chat.handler.dto.SendMessageStatus;
import com.ddang.ddang.chat.presentation.dto.request.CreateMessageRequest;
import com.ddang.ddang.chat.presentation.dto.request.ReadMessageRequest;
import com.ddang.ddang.websocket.handler.WebSocketHandleTextMessageProvider;
import com.ddang.ddang.websocket.handler.dto.ChattingType;
import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.ddang.ddang.websocket.handler.dto.SessionAttributeDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandleTextMessageProvider implements WebSocketHandleTextMessageProvider {

    private static final String CHATROOM_ID_KEY = "chatRoomId";

    private final WebSocketChatSessions sessions;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final ApplicationEventPublisher messageNotificationEventPublisher;
    private final ApplicationEventPublisher messageLogEventPublisher;

    @Override
    public TextMessageType supportTextMessageType() {
        return TextMessageType.CHATTINGS;
    }

    @Override
    public List<SendMessageDto> handleCreateSendMessage(
            final WebSocketSession session,
            final Map<String, String> data
    ) throws JsonProcessingException {
        final SessionAttributeDto sessionAttribute = getSessionAttributes(session);
        final long chatRoomId = getChatRoomId(data);
        sessions.add(session, chatRoomId);

        final ChattingType type = ChattingType.findValue(data);
        if (ChattingType.PING == type) {
            return createPingResponse(sessionAttribute, data, session);
        }
        return createSendMessageResponse(data, sessionAttribute);
    }

    private long getChatRoomId(final Map<String, String> data) {
        return Long.parseLong(data.get(CHATROOM_ID_KEY));
    }

    private SessionAttributeDto getSessionAttributes(final WebSocketSession session) {
        final Map<String, Object> attributes = session.getAttributes();

        return objectMapper.convertValue(attributes, SessionAttributeDto.class);
    }

    private List<SendMessageDto> createPingResponse(final SessionAttributeDto sessionAttribute, final Map<String, String> data, final WebSocketSession userSession) throws JsonProcessingException {
        final ChatPingDataDto pingData = objectMapper.convertValue(data, ChatPingDataDto.class);
        final ReadMessageRequest readMessageRequest = new ReadMessageRequest(sessionAttribute.userId(), pingData.chatRoomId(), pingData.lastMessageId());
        final List<ReadMessageDto> readMessageDtos = messageService.readAllByLastMessageId(readMessageRequest);

        final List<MessageDto> messageDtos = convertToMessageDto(readMessageDtos, userSession);
        final HandleMessageResponse handleMessageResponse = new HandleMessageResponse(SendMessageStatus.SUCCESS, messageDtos);
        return List.of(new SendMessageDto(userSession, new TextMessage(objectMapper.writeValueAsString(handleMessageResponse))));
    }

    private List<MessageDto> convertToMessageDto(final List<ReadMessageDto> readMessageDtos, final WebSocketSession session) {
        return readMessageDtos.stream()
                                .map(readMessageDto -> MessageDto.of(readMessageDto, isMyMessage(session, readMessageDto.writerId())))
                                .toList();
    }

    private List<SendMessageDto> createSendMessageResponse(final Map<String, String> data, final SessionAttributeDto sessionAttribute) throws JsonProcessingException {
        final Long writerId = sessionAttribute.userId();
        final ChatMessageDataDto messageData = objectMapper.convertValue(data, ChatMessageDataDto.class);
        final CreateMessageDto createMessageDto = createMessageDto(messageData, writerId);
        final Message message = messageService.create(createMessageDto);
        sendNotificationIfReceiverNotInSession(message, sessionAttribute);

        return createSendMessages(message, writerId, createMessageDto.chatRoomId());
    }

    private CreateMessageDto createMessageDto(final ChatMessageDataDto messageData, final Long userId) {
        final CreateMessageRequest request = new CreateMessageRequest(
                messageData.receiverId(),
                messageData.contents()
        );

        return CreateMessageDto.of(userId, messageData.chatRoomId(), request);
    }

    private void sendNotificationIfReceiverNotInSession(
            final Message message,
            final SessionAttributeDto sessionAttribute
    ) {
        if (!sessions.containsByUserId(message.getChatRoom().getId(), message.getReceiver().getId())) {
            final String profileImageAbsoluteUrl = String.valueOf(sessionAttribute.baseUrl());
            messageNotificationEventPublisher.publishEvent(new MessageNotificationEvent(
                    message,
                    profileImageAbsoluteUrl
            ));
        }
    }

    private List<SendMessageDto> createSendMessages(
            final Message message,
            final Long writerId,
            final Long chatRoomId
    ) throws JsonProcessingException {
        final Set<WebSocketSession> groupSessions = sessions.getSessionsByChatRoomId(message.getChatRoom().getId());

        final List<SendMessageDto> sendMessageDtos = new ArrayList<>();
        for (final WebSocketSession currentSession : groupSessions) {
            final MessageDto messageDto = MessageDto.of(message, isMyMessage(currentSession, writerId));
            final TextMessage textMessage = createTextMessage(messageDto);
            sendMessageDtos.add(new SendMessageDto(currentSession, textMessage));
            updateReadMessageLog(currentSession, chatRoomId, message);
        }

        return sendMessageDtos;
    }

    private TextMessage createTextMessage(
            final MessageDto messageDto
    ) throws JsonProcessingException {
        final HandleMessageResponse handleMessageResponse = new HandleMessageResponse(SendMessageStatus.SUCCESS, List.of(messageDto));

        return new TextMessage(objectMapper.writeValueAsString(handleMessageResponse));
    }

    private boolean isMyMessage(final WebSocketSession session, final Long writerId) {
        final long userId = Long.parseLong(String.valueOf(session.getAttributes().get("userId")));

        return writerId.equals(userId);
    }

    private void updateReadMessageLog(
            final WebSocketSession currentSession,
            final Long chatRoomId,
            final Message message
    ) {
        final SessionAttributeDto sessionAttributes = getSessionAttributes(currentSession);
        final UpdateReadMessageLogEvent updateReadMessageLogEvent = new UpdateReadMessageLogEvent(
                sessionAttributes.userId(),
                chatRoomId,
                message.getId()
        );
        messageLogEventPublisher.publishEvent(updateReadMessageLogEvent);
    }

    @Override
    public void remove(final WebSocketSession session) {
        sessions.remove(session);
    }
}
