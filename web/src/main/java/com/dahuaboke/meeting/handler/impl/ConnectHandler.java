package com.dahuaboke.meeting.handler.impl;

import com.dahuaboke.meeting.enums.MessageType;
import com.dahuaboke.meeting.handler.Handler;
import com.dahuaboke.meeting.model.Message;
import com.dahuaboke.meeting.task.AddUserTask;
import com.dahuaboke.meeting.util.RoomUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;

import static com.dahuaboke.meeting.util.AlertUtil.alert;

/**
 * @author dahua
 * @time 2021/9/20 22:38
 */
public class ConnectHandler implements Handler {
    @Override
    public void handle(Map<String, Map<String, Message>> rooms, Message message, Channel channel) {
        String id = message.getRoomId();
        if (!rooms.containsKey(id)) {
            alert("error room", channel);
            return;
        }
        rooms.get(id).forEach((k, connect) -> {
            if (connect.isRoot()) {
                message.setType(MessageType.OFFER);
                message.setFromUserId(channel.id().asShortText());
                message.setChannel(channel);
                connect.getChannel().writeAndFlush(new TextWebSocketFrame(message.toString()));
            }
        });
        rooms.get(id).put(channel.id().asShortText(), message);
        RoomUtil.getUsersFromRoom(rooms.get(id), channel);
        //后续修改为线程池
        if(!message.isCamera()){
            new Thread(new AddUserTask(rooms.get(id), channel.id().asShortText(), message.getUsername())).start();
        }
    }
}
