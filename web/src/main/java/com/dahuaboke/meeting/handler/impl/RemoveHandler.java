package com.dahuaboke.meeting.handler.impl;

import com.dahuaboke.meeting.handler.Handler;
import com.dahuaboke.meeting.model.Message;
import com.dahuaboke.meeting.task.RemoveUserTask;
import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.Map;

import static com.dahuaboke.meeting.util.AlertUtil.alert;

/**
 * @author dahua
 * @time 2021/9/20 22:53
 */
public class RemoveHandler implements Handler {
    @Override
    public void handle(Map<String, Map<String, Message>> rooms, Message m, Channel channel) {
        Iterator<Map.Entry<String, Map<String, Message>>> room = rooms.entrySet().iterator();
        while (room.hasNext()) {
            Map.Entry<String, Map<String, Message>> next = room.next();
            Iterator<Map.Entry<String, Message>> child = next.getValue().entrySet().iterator();
            while (child.hasNext()) {
                Map.Entry<String, Message> next1 = child.next();
                Message message = next1.getValue();
                if (channel.equals(message.getChannel())) {
                    if (message.isRoot()) {
                        rooms.get(message.getRoomId()).forEach((k, v) -> {
                            alert("close", v.getChannel());
                        });
                        room.remove();
                    } else {
                        String removeId = next1.getKey();
                        child.remove();
                        //通知房间其他人 这里应该用线程池
                        new Thread(new RemoveUserTask(rooms.get(next.getKey()), removeId)).start();
                    }
                }
            }
        }
    }
}
