package com.dahuaboke.meeting.task;

import com.dahuaboke.meeting.handler.Handler;
import com.dahuaboke.meeting.model.Message;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * @author dahua
 * @time 2021/9/22 17:17
 */
public class HandlerTask implements Runnable{

    private Handler handler;
    private Map<String, Map<String, com.dahuaboke.meeting.model.Message>> rooms;
    private Message message;
    private Channel channel;

    public HandlerTask(Handler handler, Map<String, Map<String, Message>> rooms, Message message, Channel channel) {
        this.handler = handler;
        this.rooms = rooms;
        this.message = message;
        this.channel = channel;
    }

    @Override
    public void run() {
        handler.handle(rooms,message,channel);
    }
}
