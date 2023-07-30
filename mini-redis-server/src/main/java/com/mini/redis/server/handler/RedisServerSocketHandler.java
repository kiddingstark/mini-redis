package com.mini.redis.server.handler;

import com.mini.redis.server.service.RedisServerService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/27 21:04
 */
@Slf4j
public class RedisServerSocketHandler extends ChannelInboundHandlerAdapter {

    private final RedisServerService redisServerService;

    public RedisServerSocketHandler(RedisServerService redisServerService){
        this.redisServerService = redisServerService;
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientIp = ctx.channel().remoteAddress().toString();
        redisServerService.connect(clientIp);
        log.info("client connect server success! clentIp:{}", clientIp);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

}
