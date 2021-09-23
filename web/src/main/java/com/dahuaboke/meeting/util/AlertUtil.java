package com.dahuaboke.meeting.util;

import com.dahuaboke.meeting.enums.MessageType;
import com.dahuaboke.meeting.model.Message;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author dahua
 * @time 2021/9/20 22:48
 */
public class AlertUtil {

    public static void alert(String mes, Channel channel) {
        Message m = new Message();
        m.setType(MessageType.ALERT);
        m.setObj(mes);
        channel.writeAndFlush(new TextWebSocketFrame(m.toString()));
    }
}
