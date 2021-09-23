package com.dahuaboke.meeting.handler.impl;

import com.dahuaboke.meeting.handler.Handler;
import com.dahuaboke.meeting.model.Message;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;

/**
 * @author dahua
 * @time 2021/9/20 22:39
 */
public class AnswerHandler implements Handler {
    @Override
    public void handle(Map<String, Map<String, Message>> rooms, Message message, Channel channel) {
        String id = message.getRoomId();
        Message root = rooms.get(id).get(channel.id().asShortText());
        if (root.isRoot()) {
            Message answer = rooms.get(id).get(message.getFromUserId());
            answer.getChannel().writeAndFlush(new TextWebSocketFrame(message.toString()));
        } else {
            rooms.get(id).forEach((k, v) -> {
                if (v.isRoot()) {
                    message.setFromUserId(channel.id().asShortText());
                    v.getChannel().writeAndFlush(new TextWebSocketFrame(message.toString()));
                }
            });
        }
    }
}
