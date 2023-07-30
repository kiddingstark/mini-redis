# mini-redis
单机版的简易redis，自己实现轮子以加深对redis原理的理解

## 实现功能

1、实现基于Netty的client与server的基本通信。

2、实现Sds、ZipList、IntSet、ZSkipList等底层结构及基本命令。

3、实现了定期删除+惰性删除的过期策略；random、lru、lfu等内存淘汰策略。

4、实现了AOF持久化机制

## 模块

### mini-redis-server
### mini-redis-client

## 启动

1、启动MiniRedisServerApplication
2、启动RedisClient，连接RedisServer，直接在控制台输入redis命令即可
