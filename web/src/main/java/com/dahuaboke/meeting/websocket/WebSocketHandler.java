package com.dahuaboke.meeting.websocket;

import com.alibaba.fastjson.JSON;
import com.dahuaboke.meeting.handler.Handler;
import com.dahuaboke.meeting.handler.impl.*;
import com.dahuaboke.meeting.model.Message;
import com.dahuaboke.meeting.task.HandlerTask;
import com.dahuaboke.meeting.util.AlertUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dahua
 * @time 2021/9/16 16:59
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String, Map<String, Message>> rooms = new ConcurrentHashMap();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) {
        String text = textWebSocketFrame.text();
        Message message = JSON.parseObject(text, Message.class);
        Channel channel = channelHandlerContext.channel();
        if (message.getRoomId() == null) {
            AlertUtil.alert("房间号不符合规范", channel);
            return;
        }
        switch (message.getType()) {
            case ONE:
                break;
            case GROUP:
                break;
            case CREATE:
                Handler create = new CreateHandler();
                new Thread(new HandlerTask(create, rooms, message, channel)).start();
                break;
            case CONNECT:
                Handler connect = new ConnectHandler();
                new Thread(new HandlerTask(connect, rooms, message, channel)).start();
                break;
            case ANSWER:
                Handler answer = new AnswerHandler();
                new Thread(new HandlerTask(answer, rooms, message, channel)).start();
                break;
            case CANDIDATE:
                Handler candidate = new CandidateHandler();
                new Thread(new HandlerTask(candidate, rooms, message, channel)).start();
                break;
            default:
                break;
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        clients.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Handler remove = new RemoveHandler();
        remove.handle(rooms, null, channel);
        clients.remove(channel);
    }
}
