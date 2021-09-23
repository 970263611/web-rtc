package com.dahuaboke.meeting.util;

import com.alibaba.fastjson.JSON;
import com.dahuaboke.meeting.enums.MessageType;
import com.dahuaboke.meeting.model.Message;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dahua
 * @time 2021/9/20 23:25
 */
public class RoomUtil {

    public static void getUsersFromRoom(Map<String, Message> room, Channel channel) {
        LinkedHashMap<String, String> all = new LinkedHashMap();
        LinkedHashMap<String, String> idAndName = new LinkedHashMap();
        room.forEach((k, v) -> {
            String id = v.getChannel().id().asShortText();
            String username = v.getUsername();
            if (!k.equals(channel.id().asShortText())) {
                idAndName.put(id, username);
            } else {
                all.put(id, username);
            }
        });
        Message message = new Message();
        message.setType(MessageType.ROOM_GET_USER);
        all.putAll(idAndName);
        message.setObj(JSON.toJSONString(all));
        channel.writeAndFlush(new TextWebSocketFrame(message.toString()));
    }
}
