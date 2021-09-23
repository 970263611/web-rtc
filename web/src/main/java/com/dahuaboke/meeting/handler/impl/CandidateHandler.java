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
public class CandidateHandler implements Handler {

    @Override
    public void handle(Map<String, Map<String, Message>> rooms, Message message, Channel channel) {
        String id = message.getRoomId();
        if (!rooms.isEmpty()) {
            Message candidate = rooms.get(id).get(channel.id().asShortText());
            if (candidate.isRoot()) {
                if (!message.isCamera()) {
                    String fromUserId = message.getFromUserId();
                    Message m = rooms.get(id).get(fromUserId);
                    m.getChannel().writeAndFlush(new TextWebSocketFrame(message.toString()));
                } else {
                    rooms.get(id).forEach((k, v) -> {
                        if (!v.isRoot()) {
                            Channel c = v.getChannel();
                            if (c.isOpen()) {
                                c.writeAndFlush(new TextWebSocketFrame(message.toString()));
                            }
                        }
                    });
                }
            } else {
                rooms.get(id).forEach((k, v) -> {
                    if (v.isRoot()) {
                        v.getChannel().writeAndFlush(new TextWebSocketFrame(message.toString()));
                    }
                });
            }
        }
    }
}
