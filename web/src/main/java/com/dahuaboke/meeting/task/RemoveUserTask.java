package com.dahuaboke.meeting.task;

import com.dahuaboke.meeting.enums.MessageType;
import com.dahuaboke.meeting.model.Message;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;

/**
 * @author dahua
 * @time 2021/9/20 22:57
 */
public class RemoveUserTask implements Runnable {

    private Map<String, Message> room;
    private String removeId;

    public RemoveUserTask(Map<String, Message> room, String removeId) {
        this.room = room;
        this.removeId = removeId;
    }

    @Override
    public void run() {
        room.forEach((k, v) -> {
            if (!k.equals(removeId)) {
                Message message = new Message();
                message.setType(MessageType.ROOM_REMOVE_USER);
                message.setObj(removeId);
                v.getChannel().writeAndFlush(new TextWebSocketFrame(message.toString()));
            }
        });
    }
}
