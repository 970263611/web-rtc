package com.dahuaboke.meeting.handler;

import com.dahuaboke.meeting.model.Message;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * @author dahua
 * @time 2021/9/16 17:09
 */
public interface Handler {

    void handle(Map<String, Map<String, Message>> rooms, Message message, Channel channel);
}
