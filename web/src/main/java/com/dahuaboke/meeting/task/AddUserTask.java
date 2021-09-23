package com.dahuaboke.meeting.task;

import com.alibaba.fastjson.JSON;
import com.dahuaboke.meeting.enums.MessageType;
import com.dahuaboke.meeting.model.Message;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dahua
 * @time 2021/9/20 22:57
 */
public class AddUserTask implements Runnable {

    private Map<String, Message> room;
    private String addId;
    private String addUsername;

    public AddUserTask(Map<String, Message> room, String addId, String addUsername) {
        this.room = room;
        this.addId = addId;
        this.addUsername = addUsername;
    }

    @Override
    public void run() {
        room.forEach((k, v) -> {
            if (!k.equals(addId)) {
                Message message = new Message();
                message.setType(MessageType.ROOM_ADD_USER);
                message.setObj(JSON.toJSONString(new HashMap() {{
                    put(addId, addUsername);
                }}));
                v.getChannel().writeAndFlush(new TextWebSocketFrame(message.toString()));
            }
        });
    }
}
