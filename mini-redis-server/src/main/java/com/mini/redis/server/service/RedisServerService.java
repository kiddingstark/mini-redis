package com.mini.redis.server.service;

import com.mini.redis.server.config.ApplicationContextConfig;
import com.mini.redis.server.constant.RedisCommand;
import com.mini.redis.server.constant.ResultCode;
import com.mini.redis.server.struct.RedisClient;
import com.mini.redis.server.struct.RedisServer;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author: zhengruihong
 * @description
 * @date: 2023/7/27 20:44
 */
@Service
public class RedisServerService {

    @Autowired
    private ApplicationContextConfig applicationContextConfig;

    /**
     * connect
     *
     * @param clientIp
     */
    public void connect(String clientIp) {
        RedisServer redisServer = RedisServer.getRedisServer();
        List<RedisClient> redisClients = redisServer.getClients();
        if (CollectionUtils.isNotEmpty(redisClients) && redisClients.size() == redisServer.getMaxClientNum()) {
            return;
        }
        RedisClient redisClient = RedisClient.builder()
                .clientIp(clientIp)
                .currentDb(redisServer.getRedisDbs().get(0))
                .build();
        redisClients.add(redisClient);
        redisServer.setClients(redisClients);
    }

    /**
     * processCommand
     *
     * @param clientIp
     * @param processCommand
     * @return
     */
    public String processCommand(String clientIp, String processCommand) {
        String[] argv = processCommand.split(" ");
        RedisCommand redisCommand = RedisCommand.getRedisCommandByCommandType(argv[0]);
        if (Objects.isNull(redisCommand)) {
            return String.format(ResultCode.UNKOWN_COMMAND.getMsg(), argv[0]);
        }

        String validateCommand = this.validateCommand(redisCommand, argv);
        if (!validateCommand.equals(ResultCode.SUCCEED.getMsg())) {
            return validateCommand;
        }

        Class<?> serviceClazz = RedisCommandService.class;
        String serviceMethod = redisCommand.getServiceMethod();

        String result = "";
        try {
            Method method = serviceClazz.getMethod(serviceMethod, RedisClient.class, String[].class);
            result = (String) method.invoke(applicationContextConfig.getBean(serviceClazz), this.getRedisClient(clientIp), argv);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return ResultCode.ERROR.getMsg();
        }

        return result;
    }


    /**
     * validate argv number
     *
     * @param redisCommand
     * @param argv
     * @return
     */
    private String validateCommand(RedisCommand redisCommand, String[] argv) {
        String commandType = argv[0];
        if (Objects.isNull(redisCommand)) {
            return String.format(ResultCode.UNKOWN_COMMAND.getMsg(), commandType);
        }
        Integer arity = redisCommand.getArity();
        if ((arity > 0 && argv.length != arity)
                || (arity < 0 && Math.abs(arity) > argv.length)) {
            return String.format(ResultCode.WRONG_NUMBER_ARFUMENTS.getMsg(), commandType);
        }
        return ResultCode.SUCCEED.getMsg();
    }

    /**
     * getRedisClient by clientIp
     *
     * @param clientIp
     * @return
     */
    private RedisClient getRedisClient(String clientIp) {
        RedisServer redisServer = RedisServer.getRedisServer();
        List<RedisClient> redisClients = redisServer.getClients();
        if (CollectionUtils.isEmpty(redisClients)) {
            return null;
        }
        return redisClients.stream()
                .filter(v -> v.getClientIp().equals(clientIp))
                .findFirst()
                .orElse(null);
    }
}
