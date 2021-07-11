package client;

import common.store.StoreObject;
import jdk.jfr.Unsigned;
import lombok.Data;

import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName PandisClient
 * @Description PandisClient 保存了客户端当前的状态信息，以及执行相关功能时需要用到的数据结构
 * @Author huangyaohua
 * @Date 2021-07-11 19:52
 * @Version
 */
public class PandisClient {

    private final int REDIS_REPLY_CHUNK_BYTES = 16*1024;

    // 套接字描述符
    private SocketChannel fd;

    // 当前正在使用的数据库
//     PandisDb db;

    // 当前正在使用的数据库的 id （号码）
//    int dictid;

    // 客户端的名字
    private StoreObject name;             /* As set by CLIENT SETNAME */

    // 查询缓冲区
//    sds querybuf;

    // 查询缓冲区长度峰值
    private int queryBufPeak;   /* Recent (100ms or more) peak of querybuf size */

    // 参数数量
    private int argc;

    // 参数对象数组
    private StoreObject[] argv;

    // 记录被客户端执行的命令
    private PandisCommand cmd, lastCmd;

    // 请求的类型：内联命令还是多条命令
    private int reqType;

    // 剩余未读取的命令内容数量
    private int multiBulkLen;       /* number of multi bulk arguments left to read */

    // 命令内容的长度
    private long bulkLen;           /* length of bulk argument in multi bulk request */

    // 回复链表
    private List<StoreObject> reply;

    // 回复链表中对象的总大小
    private long replyBytes; /* Tot bytes of objects in reply list */

    // 已发送字节，处理 short write 用
    private int sentLen;            /* Amount of bytes already sent in the current buffer or object being sent. */

    // 创建客户端的时间
    private Date createTime;           /* Client creation time */

    // 客户端最后一次和服务器互动的时间
    private Date lastInteraction; /* time of the last interaction, used for timeout */

    // 客户端的输出缓冲区超过软性限制的时间
    private Date oBufSoftLimitReachedTime;

    // 客户端状态标志
    private int flags;              /* REDIS_SLAVE | REDIS_MONITOR | REDIS_MULTI ... */

    // 当 server.requirepass 不为 NULL 时
    // 代表认证的状态
    // 0 代表未认证， 1 代表已认证
//    int authenticated;      /* when requirepass is non-NULL */

    // 复制状态
//    int replstate;          /* replication state if this is a slave */
    // 用于保存主服务器传来的 RDB 文件的文件描述符
//    int repldbfd;           /* replication DB file descriptor */

    // 读取主服务器传来的 RDB 文件的偏移量
//    off_t repldboff;        /* replication DB file offset */
    // 主服务器传来的 RDB 文件的大小
//    off_t repldbsize;       /* replication DB file size */

//    sds replpreamble;       /* replication DB preamble. */

    // 主服务器的复制偏移量
//    long long reploff;      /* replication offset if this is our master */
    // 从服务器最后一次发送 REPLCONF ACK 时的偏移量
//    long long repl_ack_off; /* replication ack offset, if this is a slave */
    // 从服务器最后一次发送 REPLCONF ACK 的时间
//    long long repl_ack_time;/* replication ack time, if this is a slave */
    // 主服务器的 master run ID
    // 保存在客户端，用于执行部分重同步
//    char replrunid[REDIS_RUN_ID_SIZE+1]; /* master run id if this is a master */
    // 从服务器的监听端口号
//    int slave_listening_port; /* As configured with: SLAVECONF listening-port */

    // 事务状态
//    multiState mstate;      /* MULTI/EXEC state */

    // 阻塞类型
//    int btype;              /* Type of blocking op if REDIS_BLOCKED. */
    // 阻塞状态
//    blockingState bpop;     /* blocking state */

    // 最后被写入的全局复制偏移量
//    long long woff;         /* Last write global replication offset. */

    // 被监视的键
//    list *watched_keys;     /* Keys WATCHED for MULTI/EXEC CAS */

    // 这个字典记录了客户端所有订阅的频道
    // 键为频道名字，值为 NULL
    // 也即是，一个频道的集合
//    dict *pubsub_channels;  /* channels a client is interested in (SUBSCRIBE) */

    // 链表，包含多个 pubsubPattern 结构
    // 记录了所有订阅频道的客户端的信息
    // 新 pubsubPattern 结构总是被添加到表尾
//    list *pubsub_patterns;  /* patterns a client is interested in (SUBSCRIBE) */
//    sds peerid;             /* Cached peer ID. */

    /* Response buffer */
    // 回复偏移量
    int bufPos;
    // 回复缓冲区
    private char[] buf = new char[REDIS_REPLY_CHUNK_BYTES];

    public static PandisClient createClient(SocketChannel fd){
        PandisClient ps = new PandisClient();
        // 当 fd 为 true 时，创建带网络连接的客户端
        // 如果 fd 为 false 时 ，那么创建无网络连接的伪客户端
        // 因为 Redis 的命令必须在客户端的上下文中使用，所以在执行 Lua 环境中的命令时
        // 需要用到这种伪终端

        //初始化属性
        ps.fd = fd;
        ps.name = null;
        ps.bufPos = 0;
//        ps.queryBuf = sdsEmpty();
        ps.queryBufPeak = 0;
        ps.reqType = 0;
        ps.argc = 0;
        ps.argv = null;
        ps.cmd = ps.lastCmd = null;
        ps.multiBulkLen = 0;
        ps.bulkLen = -1;
        ps.sentLen = 0;
        ps.flags = 0;
        ps.createTime = ps.lastInteraction = new Date();
        ps.reply = new LinkedList<>();
        ps.replyBytes = 0;
        ps.oBufSoftLimitReachedTime = new Date(0);
        // 如果不是伪客户端，那么添加到服务器的客户端链表中
        if (fd.isConnected()) {
            //TODO
        }
        // 初始化客户端的事务状态
        // TODO
        // 返回客户端
        return ps;
    }

}
