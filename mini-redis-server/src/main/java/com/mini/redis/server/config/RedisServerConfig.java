package com.mini.redis.server.config;

import com.mini.redis.server.handler.RedisServerMessageHandler;
import com.mini.redis.server.handler.RedisServerSocketHandler;
import com.mini.redis.server.service.RedisServerService;
import com.mini.redis.server.struct.RedisServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/11 16:57
 */
@Configuration
@Slf4j
public class RedisServerConfig {

    @Autowired
    private RedisServerService redisServerService;

    @PostConstruct
    public void initConnect() {
        RedisServer redisServer = RedisServer.getRedisServer();
        EventLoopGroup bossGroup = new NioEventLoopGroup(); //reactor 负责连接注册
        EventLoopGroup workerGroup = new NioEventLoopGroup();// 工作线程池 负责事件的读写及业务处理
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RedisServerSocketHandler(redisServerService));
                            ch.pipeline().addLast(new RedisServerMessageHandler(redisServerService));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(redisServer.getIp(), redisServer.getPort())
                    .sync();

            log.info("redis server socker waiting for connect! ip:{}, port:{}", redisServer.getIp(), redisServer.getPort());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
