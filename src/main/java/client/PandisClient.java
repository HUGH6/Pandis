package client;

import command.AbstractCommand;
import command.Command;
import command.CommandExecutor;
import command.instance.AuthCommand;
import common.store.Sds;
import common.store.StoreObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import protocol.RequestProcessor;
import protocol.RequestType;
import server.PandisDatabase;
import server.PandisServer;

import java.io.IOException;
import java.nio.ByteBuffer;
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
    private static Log logger = LogFactory.getLog(PandisClient.class);

    private final int REDIS_REPLY_CHUNK_BYTES = 16*1024;

    // 套接字描述符
    private SocketChannel socketChannel;

    // 当前正在使用的数据库
    private PandisDatabase database;
    // 当前正在使用的数据库的id
    private int databaseId;

    // 客户端的名字
    private StoreObject name;             /* As set by CLIENT SETNAME */

    // 查询缓冲区
    private Sds queryBuffer;

    // padis新增，用来从socketChannel中读取数据
    private ByteBuffer socketBuffer;

    // 查询缓冲区长度峰值
    private int queryBufPeak;   /* Recent (100ms or more) peak of querybuf size */

    // 参数数量
    private int argc;

    // 参数对象数组
    private StoreObject[] argv;

    // 记录被客户端执行的命令
    private AbstractCommand cmd, lastCmd;

    // 请求的类型：内联命令还是多条命令
    private RequestType requestType;

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
    boolean authenticated;

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

    public static PandisClient createClient(SocketChannel socketChannel){
        PandisClient ps = new PandisClient();
        // 当 fd 为 true 时，创建带网络连接的客户端
        // 如果 fd 为 false 时 ，那么创建无网络连接的伪客户端
        // 因为 Redis 的命令必须在客户端的上下文中使用，所以在执行 Lua 环境中的命令时
        // 需要用到这种伪终端

        //初始化属性
        ps.socketChannel = socketChannel;
        ps.socketBuffer = ByteBuffer.allocate(8);
        ps.queryBuffer = Sds.newEmptySds();
        ps.requestType = RequestType.NONE; // 请求类型，默认为0，表示没有类型

        // 设置默认数据库
        ps.selectDatabase(PandisServer.getInstance().getDatabases().get(0), 0);

        ps.authenticated = false;

        ps.name = null;
        ps.bufPos = 0;
        ps.queryBufPeak = 0;
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
//        if (fd.isConnected()) {
            //TODO
//        }
        // 初始化客户端的事务状态
        // TODO
        // 返回客户端
        return ps;
    }


    /**
     * 从客户端对应的SocketChannel中读取数据到客户端的查询缓冲区
     * @return 返回一个int值。返回值为-1表示客户端已经关闭连接，返回值为正数表示读取的字节数，0表示异常情况
     */
    public int readSocketData() {

        int bytesCount = 0;
        try {
            this.socketBuffer.clear();
            int byteRead = this.socketChannel.read(this.socketBuffer);

            while (byteRead > 0) {
                bytesCount += byteRead;

                // 将读取的数据写入查询缓冲区
                this.socketBuffer.flip();
                while (this.socketBuffer.hasRemaining()) {
                    byte [] temp = new byte[this.socketBuffer.remaining()];
                    this.socketBuffer.get(temp);
                    this.queryBuffer.cat(temp);
                }

                this.socketBuffer.clear();
                byteRead = this.socketChannel.read(this.socketBuffer);
            };

            // 正常读取了数据，直接返回
            if (bytesCount > 0) {
                return bytesCount;
            }

            // 客户端关闭连接，返回-1
            if (byteRead == -1) {
                return -1;
            }

        } catch (IOException e) {
            logger.error("Read from SocketChannel error", e);
        }

        // 异常情况，返回0
        return 0;
    }

    /**
     * 处理查询缓冲区的数据
     */
    public void processInputBuffer() {
        while (!this.queryBuffer.isEmpty()) {
            // 这里可能需要对客户端的各种状态进行判断
            // todo

            // 判断请求的类型
            // 两种类型的区别可以在 Redis 的通讯协议上查到：
            // 简单来说，多条查询是一般客户端发送来的，
            // 而内联查询则是 TELNET 发送来的
            if (this.requestType != RequestType.NONE) {
                if (this.queryBuffer.charAt(0) == '*') {
                    // 多条查询
                    this.requestType = RequestType.MULTI_BULK;
                } else {
                    // 内联查询
                    this.requestType = RequestType.INLINE;
                }
            }

            // 将缓冲区的数据转换命令及命令参数
            if (this.requestType == RequestType.INLINE) {
                if (!RequestProcessor.processInlineRequest(this)) {
                    break;
                }
            } else if (this.requestType == RequestType.MULTI_BULK) {
                if (!RequestProcessor.processMultiBulkRequest(this)) {
                    break;
                }
            } else {
                logger.error("Unknow request type");
            }
        }
    }

    /**
     * 处理从客户端数据中解析出的命令参数，执行相应的命令
     *
     * 这个函数执行时，我们已经读入了一个完整的命令到客户端，
     * 这个函数负责执行这个命令，
     * 或者服务器准备从客户端中进行一次读取。
     */
    public void processCommand() {
        // 目前从客户端输入的数据中解析出了参数及其数量，argv、argc
        // 但在实际执行相应命令前还要进行一些检查操作

        // 单独处理quit命令
        // if (argv[0].getObj().toString().equals("quit")) {
        //    return;
        // }

        // (1)检查用户输入的命令名称是否可以找到对应命令实现，
        // 如果找不到相应的命令实现，服务器不再执行后续步骤，并向客户端返回一个错误。
        // (2)根据命令名称获得的命令实现，可以获得该命令arity属性，
        // 检查命令请求所给定的参数个数是否正确，当参数个数不正确时，不再执行后续步骤，直接向客户端返回一个错误。
        String commandName = argv[0].getObj().toString();
        AbstractCommand command = CommandExecutor.lookupCommand(commandName);
        if (command == null) {
            // 没找到命令
            // 回复错误信息
            return;
        } else if ((!command.isGreaterThanArity() && command.getArity() != argc) || argc < command.getArity()) {
            // 参数个数错误
            // 回复错误信息
            return;
        }

        // (3)检查客户端是否已经通过了身份验证，
        // 未通过身份验证的客户端只能执行AUTH命令，
        // 如果未通过身份验证的客户端试图执行除AUTH命令之外的其他命令，那么服务器将向客户端返回一个错误。
        if (PandisServer.getInstance().getServerConfig().getRequirePassword() != null
            && !this.authenticated
            && ! (command instanceof AuthCommand)) {
            // 回复错误信息
            return;
        }

        // (4) [暂时不实现]
        // 如果服务器打开了maxmemory功能，
        // 那么在执行命令之前，先检查服务器的内存占用情况，并在有需要时进行内存回收，从而使得接下来的命令可以顺利执行。
        // 如果内存回收失败，那么不再执行后续步骤，向客户端返回一个错误。

        // (5) [暂时不实现]
        // 如果服务器上一次执行BGSAVE命令时出错，
        // 并且服务器打开了stop-writes-on-bgsaveerror功能，
        // 而且服务器即将要执行的命令是一个写命令，那么服务器将拒绝执行这个命令， 并向客户端返回一个错误。

        // (6) [暂时不实现]
        // 如果客户端当前正在用SUBSCRIBE命令订阅频道，或者正在用PSUBSCRIBE命令订阅模式，
        // 那么服务器只会执行客户端发来的SUBSCRIBE、PSUBSCRIBE、UNSUBSCRIBE、 PUNSUBSCRIBE四个命令，其他命令都会被服务器拒绝。

        // (7) [暂时不实现]
        // 如果服务器正在进行数据载入，
        // 那么客户端发送的命令必须带有l标识（比如INFO、 SHUTDOWN、PUBLISH等等）才会被服务器执行，其他命令都会被服务器拒绝。

        // (8) [暂时不实现]
        // 如果客户端正在执行事务，
        // 那么服务器只会执行客户端发来的EXEC、DISCARD、 MULTI、WATCH四个命令，其他命令都会被放进事务队列中。

        // (9) [暂时不实现]
        // 如果服务器打开了监视器功能，那么服务器会将要执行的命令和参数等信息发送给监视器。

        // 当完成了以上预备操作之后，服务器就可以开始真正执行命令了

        // [暂时不实现] 判断是否是事务模式，如果是就将命令加入队列中
        // 否则直接执行

        CommandExecutor.execute(command);



    }
    /**
     * 销毁客户端，清理资源
     */
    public void distroy() {
        try {
            this.socketChannel.close();
        } catch (IOException e) {
            logger.warn("Close client socket error.");
        }
    }

    /**
     * 为客户端选择数据库
     * @param database
     * @param id
     */
    public void selectDatabase(PandisDatabase database, int id) {
        if (id < 0 || id >= PandisServer.getInstance().getServerConfig().getDbNumber()) {
            throw new IllegalArgumentException("the selected db id is " + id + ", out of the db number bounds.");
        }

        this.database = database;
        this.databaseId = id;
    }

    public ByteBuffer getSocketBuffer() {
        return this.socketBuffer;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
}
