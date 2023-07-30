package com.mini.redis.server.handler;

import com.mini.redis.server.service.RedisServerService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/27 11:48
 */
public class RedisServerMessageHandler extends ChannelDuplexHandler {

    private final RedisServerService redisServerService;

    public RedisServerMessageHandler(RedisServerService redisServerService) {
        this.redisServerService = redisServerService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        String command = byteBuf.toString(CharsetUtil.UTF_8);
        String clientIp = ctx.channel().remoteAddress().toString();
        String result = redisServerService.processCommand(clientIp, command);
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
        //ctx.writeAndFlush(Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.print("exceptionCaught: ");
        cause.printStackTrace(System.err);
        ctx.close();
    }
}
