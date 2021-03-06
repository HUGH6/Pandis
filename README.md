# 什么是Pandis?
Pandis是一个模仿Redis实现的分布式缓存系统，基于Java实现。

## How to run
**服务端**

运行
```
server.PandisServer.main()
```

**服务端**

运行
```
cli.ClientCli.main()
```

然后在终端中输入命令即可

# 项目架构
初步项目想要实现以下功能：
* 基本的单机存储功能，实现不同特点的kv数据存储结构
* 基于Reactor模式的事件分发系统
* 分布式功能

项目架构暂时按以下模块划分：
* event
* cache
* cluster
* common
  * store
    * 字符串
      * SDS
    * 列表
      * ZIPLIST
    * 集合
    * 哈希表
    * 有序列表
* ...

# 开发计划
## 基础架构开发（7月-8月中旬）
* 基本存储结构
    * [x] String
    * List
    * Set
    * ZSet
    * Hash
* 数据库存储功能
    * [x] 键值存储
    * [x] 键值过期清除
        * [x] 惰性清除
        * [x] 定期清除
* 网络事件处理
    * [x] 事件循环
    * [x] IO事件
    * [x] 定时事件
* 系统配置模块

## 单机功能完善（8月中旬-9月中旬）
* 客户端
    * [x] 命令行客户端初步完成
* 命令
    * [x] append
    * [x] del
    * [x] echo
    * [x] exists
    * [x] expire
    * [x] expireat
    * [x] get
    * [x] persist
    * [x] pexpire
    * [x] pexpireat
    * [x] ping
    * [x] psetex
    * [x] psubscribe
    * [x] pttl
    * [x] publish
    * [x] pubsub
    * [x] punsubscribe
    * [x] select
    * [x] set
    * [x] setex
    * [x] setnx
    * [x] strlen
    * [x] subscribe
    * [x] ttl
    * [x] unsubscribe
* 两种持久化方式
* 发布/订阅功能
    * [x] 发布订阅相关功能
