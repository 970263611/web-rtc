package com.dahuaboke.meeting.handler.impl;

import com.dahuaboke.meeting.handler.Handler;
import com.dahuaboke.meeting.model.Message;
import com.dahuaboke.meeting.util.RoomUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.dahuaboke.meeting.util.AlertUtil.alert;

/**
 * @author dahua
 * @time 2021/9/20 22:38
 */
public class CreateHandler implements Handler {
    @Override
    public void handle(Map<String, Map<String, Message>> rooms, Message message, Channel channel) {
        String id = message.getRoomId();
        if (message.isCamera()) {
            if (id == null || "".equals(id)) {
                alert("请先创建房间", channel);
                return;
            }
            StringBuffer fromUserIds = new StringBuffer();
            rooms.get(id).forEach((k, create) -> {
                if (!create.isRoot()) {
                    fromUserIds.append(create.getChannel().id().asShortText());
                    fromUserIds.append(",");
                }
            });
            message.setFromUserId(new String(fromUserIds));
            channel.writeAndFlush(new TextWebSocketFrame(message.toString()));
        } else {
            if (rooms.containsKey(id)) {
                alert("房间号重复", channel);
                return;
            }
            Message create = new Message();
            create.setRoomId(id);
            create.setUsername(message.getUsername());
            create.setChannel(channel);
            create.setRoot(true);
            rooms.put(id, new LinkedHashMap() {{
                put(channel.id().asShortText(), create);
            }});
            RoomUtil.getUsersFromRoom(rooms.get(id), channel);
        }
    }
}
