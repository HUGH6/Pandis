package client;

import command.AbstractCommand;
import command.CommandExecutor;
import command.instance.AuthCommand;
import common.store.Sds;
import common.store.PandisObject;
import event.handler.SendApplyToClientHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import protocol.ReplyType;
import protocol.RequestParser;
import protocol.RequestType;
import server.PandisDatabase;
import server.PandisServer;
import utils.SafeEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * @Description PandisClient 保存了客户端当前的状态信息，以及执行相关功能时需要用到的数据结构
 * @Author huangyaohua
 * @Date 2021-07-11 19:52
 */
public class PandisClient {
    private static Log logger = LogFactory.getLog(PandisClient.class);

    // 恢复缓冲区大小（16kb）
    public static final int REPLY_CHUNK_BYTES = 16 * 1024;

    // 套接字描述符
    private SocketChannel socketChannel;
    // pandis新增，用来从socketChannel中读取数据
    private ByteBuffer socketBuffer;

    // 查询缓冲区
    private Sds queryBuffer;

    // 当前正在使用的数据库
    private PandisDatabase database;
    // 当前正在使用的数据库的id
    private int databaseId;

    // 客户端的名字
    private String name;

    // 参数数量
    private int argc;

    // 参数对象数组
    private PandisObject[] argv;



    // 记录被客户端执行的命令
    // private AbstractCommand cmd, lastCmd;

    // 请求的类型：内联命令还是多条命令
    private volatile RequestType requestType;


    // 剩余未读取的命令内容数量
    private int multiBulkLen;       /* number of multi bulk arguments left to read */



    // 命令内容的长度
    private int bulkLen;           /* length of bulk argument in multi bulk request */

    /*******************************************************************************
     * 服务器要发送给客户端的回复信息，都会先保存在对应客户端对象的回复缓冲区或回复列表中
     * 当客户端的套接字可写时，调用写处理器，将换成的回复信息发送给客户端
     * 回复消息优先缓存在缓冲区数组中，当缓冲区数组空间不足时，则存入队列中
     *******************************************************************************/
    // 回复缓冲区
    private byte [] replyBuffer;
    // 回复缓冲区偏移量，记录当前缓冲区中所有数据的长度
    private int replyBufferPos;
    // 记录已经发送的数据长度
    private int sentBufferPos;
    // 回复缓冲队列
    private List<String> replyList;
    // 已发送字节数，处理 short write 用
    private int sentLen;            /* Amount of bytes already sent in the current buffer or object being sent. */
    // 回复链表中对象的总大小
    private long replyBytes; /* Tot bytes of objects in reply list */

    // 服务器内存限制
    public static final int MAX_WRITE_PER_EVENT = 1024 * 64;

    public int getReplyBufferPos() {
        return this.replyBufferPos;
    }


    public List<String> getReplyQueue() {
        return this.replyList;
    }



    public void setSentLen(int _sentLen) {
        this.sentLen = _sentLen;
    }
    // 创建客户端的时间
    private Date createTime;           /* Client creation time */

    // 客户端最后一次和服务器互动的时间
    private Date lastInteraction; /* time of the last interaction, used for timeout */

    public void setLastInteraction(Date date) {
        this.lastInteraction = date;
    }
    // 客户端的输出缓冲区超过软性限制的时间
    // private Date oBufSoftLimitReachedTime;

    // 客户端状态标志
    private int flags;              /* REDIS_SLAVE | REDIS_MONITOR | REDIS_MULTI ... */

    public int getFlags() {
        return this.flags;
    }
    // 当 server.requirepass 不为 NULL 时
    // 代表认证的状态
    private boolean authenticated;

    // 查询缓冲区长度峰值
    // private int queryBufPeak;   /* Recent (100ms or more) peak of querybuf size */

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

    private PandisClient() {}

    public static PandisClient createClient(SocketChannel socketChannel) {
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

        ps.replyBufferPos = 0;
        ps.sentBufferPos = 0;
        ps.replyBuffer = new byte[REPLY_CHUNK_BYTES];
        ps.replyList = new LinkedList<>();

        // 设置默认数据库
        // ps.selectDatabase(PandisServer.getInstance().getDatabases().get(0), 0);

        ps.authenticated = false;

        ps.name = null;
        // ps.queryBufPeak = 0;
        ps.argc = 0;
        ps.argv = new PandisObject[0];
        ps.multiBulkLen = 0;
        ps.bulkLen = -1;
        ps.sentLen = 0;
        ps.flags = 0;
        ps.createTime = ps.lastInteraction = new Date();
        ps.replyBytes = 0;
        // ps.oBufSoftLimitReachedTime = new Date(0);
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
     * 将缓冲区数据写入客户端对应的SocketChannel  buf->channel
     * @return 返回一个int值。返回值为-1表示客户端已经关闭连接，返回值为正数表示写入的字节数，0表示异常情况
     */
    public int writeSocketData() {
        // 发给客户端的总数据大小
        int totalWrittenNum = 0;

        // 有回复内容待发
        while (this.replyBufferPos > 0 || !this.replyList.isEmpty()) {
            // 如果缓冲区有数据，则优先发送缓冲区内的数据
            if (this.replyBufferPos > 0) {
                // 等待发送的数据先放入socketBuffer
                this.socketBuffer.clear();
                int fillLength = Math.min(this.replyBufferPos - this.sentBufferPos, this.socketBuffer.remaining());
                this.socketBuffer.put(this.replyBuffer, this.sentBufferPos, fillLength);

                // 接下来通过socketBuffer向channel中写数据
                this.socketBuffer.flip();

                int writtenSum = 0; // 单次write写入的字节数
                int writtenNum = 0; // 发送一条回复过程中的发送字节数量和
                try {
                    while (this.socketBuffer.hasRemaining()) {
                        writtenNum = this.socketChannel.write(this.socketBuffer);
                        writtenSum += writtenNum;
                        totalWrittenNum += writtenNum;
                    }
                } catch (IOException e) {
                    logger.error("Write reply to client error", e);

                    // 如果发送过程中发生异常，而消息又没有发完，则需要将这部分消息重新缓存起来
                    this.sentBufferPos += writtenNum;
                    if (this.sentBufferPos == this.replyBufferPos) {
                        this.replyBufferPos = 0;
                        this.sentBufferPos = 0;
                    }
                    return totalWrittenNum;
                }

                this.sentBufferPos += writtenSum;
                if (this.sentBufferPos == this.replyBufferPos) {
                    this.replyBufferPos = 0;
                    this.sentBufferPos = 0;
                }
            } else {
                // 发送消息队列中的消息
                String headMessage = this.replyList.get(0);
                byte [] byteMessage = SafeEncoder.encode(headMessage);

                // 该节点没有内容，直接跳过
                if (byteMessage.length == 0) {
                    this.replyList.remove(0);
                    continue;
                }

                int writtenNum = 0; // 单次write写入的字节数
                int writtenSum = 0; // 发送一条回复过程中的发送字节数量和
                int index = 0;      // 字符串的内容会分批先存入ByteBuffer，index用于标记已经写入的位置
                while (index < byteMessage.length) {
                    // 将需要写入的数据先写入辅助buf 通过buf写入到channel
                    this.socketBuffer.clear();
                    int fillLength = Math.min(byteMessage.length - index, this.socketBuffer.remaining());
                    this.socketBuffer.put(byteMessage, index, fillLength);

                    this.socketBuffer.flip();

                    try {
                        while (this.socketBuffer.hasRemaining()) {
                            writtenNum = this.socketChannel.write(this.socketBuffer);
                            writtenSum += writtenNum;
                            totalWrittenNum += writtenNum;
                        }
                    } catch (IOException e) {
                        logger.error("Write reply to client error", e);

                        // 如果发送过程中发生异常，而消息又没有发完，则需要将这部分消息重新缓存起来
                        if (writtenSum < byteMessage.length) {
                            int remainingLength = byteMessage.length - writtenSum;
                            if (this.replyBuffer.length - this.replyBufferPos > remainingLength) {
                                // 如果byte[]缓冲区有足够的空间，则将剩余内容优先放到缓冲区中，等待下次发送
                                System.arraycopy(byteMessage, writtenNum, this.replyBuffer, this.replyBufferPos, remainingLength);
                            } else {
                                // 如果缓冲区空间不够，则只能继续放在队列中
                                String remaningMessage = SafeEncoder.encode(Arrays.copyOfRange(byteMessage, writtenNum, byteMessage.length));
                                this.replyList.set(0, remaningMessage);
                            }
                        }

                        // 返回发送异常前已经发送成功的数据长度
                        return totalWrittenNum;
                    }

                    index += writtenSum;
                }

                // 执行到这里，列表头的消息已经发送成功，则将其从队列中移除
                this.replyList.remove(0);
            }

            /**
             * 为了避免一个非常大的回复独占服务器，
             * 当写入的总数量大于 PANDIS_MAX_WRITE_PER_EVENT
             * 临时中断写入，将处理时间让给其他客户端，
             * 剩余的内容等下次写入就绪再继续写入
             */
            if (totalWrittenNum > MAX_WRITE_PER_EVENT) {
                break;
            }
        }

        if(totalWrittenNum > 0) {
            return totalWrittenNum;
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
            if (this.requestType == RequestType.NONE) {
                if (this.queryBuffer.charAt(0) == RequestType.MULTI_BULK_PREFIX) {
                    // 多条查询
                    this.requestType = RequestType.MULTI_BULK;
                } else {
                    // 内联查询
                    this.requestType = RequestType.INLINE;
                }
            }

            // 将缓冲区的数据转换命令及命令参数
            if (this.requestType == RequestType.INLINE) {
                if (!RequestParser.processInlineRequest(this)) {
                    break;
                }
            } else if (this.requestType == RequestType.MULTI_BULK) {
                if (!RequestParser.processMultiBulkRequest(this)) {
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

        CommandExecutor.execute(command, this);
    }

    /*******************************************************************************
     * 以下方法用于将服务器的回复信息写入客户端结构的回复缓冲区和缓冲队列
     *******************************************************************************/

    /**
     * 这个函数在每次向客户端发送数据时都会被调用。
     *
     * 函数的行为如下：
     * (1)
     * 当客户端可以接收新数据时（通常情况下都是这样），函数返回 true，
     * 并将写处理器（write handler）注册到事件循环中，
     * 这样当套接字可写时，新数据就会被写入。
     *
     * (2)
     * 对于那些不应该接收新数据的客户端，比如伪客户端、 master 以及未 ONLINE 的 slave ，
     * 或者写处理器安装失败时，
     * 函数返回 false 。
     *
     * (3)
     * 通常在每个回复被创建时调用，如果函数返回 false ，
     * 那么没有数据会被追加到输出缓冲区。
     *
     * @return 操作是否成功
     */
    public boolean prepareClientToWrite() {
        // [暂时不实现] LUA 脚本环境所使用的伪客户端总是可写的
        // TODO

        // [暂时不实现] 客户端是主服务器并且不接受查询，那么它是不可写的，出错
        // TODO

        // 无连接的伪客户端总是不可写的
        if (this.socketChannel == null) {
            return false;
        }

        // 一般情况，为客户端套接字安装写处理器到事件循环
        PandisServer
            .getInstance()
            .getEventLoop()
            .registerFileEvent(this.socketChannel,
                                SelectionKey.OP_WRITE,
                                SendApplyToClientHandler.getHandler(),
                                this);
        return true;
    }

    /**
     * 判断是否还有没有发送的回复数据
     * @return
     */
    public boolean isReplyEmpty() {
        if (this.replyBufferPos > 0 || !this.replyList.isEmpty()) {
            return true;
        }

        return false;
    }
    /**
     * [待完善]
     * 向回复缓冲区写入指定类型的消息
     * @param replyType 回复类型
     * @param message 消息
     */
    public void addReply(ReplyType replyType, String message) {
        String reply = replyType.buildReplyByTemplate(message);
        addReply(reply);
    }

    /**
     * 将回复数据写入到客户端的回复缓冲区或者回复队列中，为服务器向客户端发送回复做准备
     * @param message
     */
    public void addReply(String message) {
        // 为客户端注册写处理器到事件循环中
        if (!prepareClientToWrite()) {
            return;
        }

        // 将回复消息写入客户端缓冲区或缓冲队列
        if (!addReplyToBuffer(message)) {
            addReplyToQueue(message);
        }
    }

    /**
     * 将回复信息缓存到客户端的回复缓冲区中
     * @param message 回复信息
     * @return 写入是否成功
     */
    private boolean addReplyToBuffer(String message) {
        // 计算回复缓冲区空余空间
        long avalible = this.replyBuffer.length - this.replyBufferPos;

        // 计算客户端状态
        // 如果正准备关闭客户端，无须再发送内容
        // TODO

        // 如果回复链表里已经有内容，再添加内容到回复缓冲区里面就是错误了
        if (this.replyList.size() > 0) {
            return false;
        }

        // 回复缓冲区的空间必须满足
        if (message.length() > avalible) {
            return false;
        }

        // 复制message到回复缓冲区里面
        byte [] byteMessage = SafeEncoder.encode(message);
        System.arraycopy(byteMessage, 0, this.replyBuffer, this.replyBufferPos, byteMessage.length);
        this.replyBufferPos += byteMessage.length;

        return true;
    }

    /**
     * 将回复消息写入回复缓冲队列
     * @param message
     */
    private void addReplyToQueue(String message) {
        // 计算客户端状态
        // 如果正准备关闭客户端，无须再发送内容
        // TODO

        this.replyList.add(message);
    }
//    public boolean addReply(String) {
//
//    }

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

    public Sds getQueryBuffer() {
        return queryBuffer;
    }

    public void setArgc(int argc) {
        this.argc = argc;
    }

    public void setArgv(PandisObject[] argv) {
        this.argv = argv;
    }

    public void setQueryBuffer(Sds queryBuffer) {
        this.queryBuffer = queryBuffer;
    }

    public int getMultiBulkLen() {
        return multiBulkLen;
    }

    public void setMultiBulkLen(int multiBulkLen) {
        this.multiBulkLen = multiBulkLen;
    }

    public int getBulkLen() {
        return bulkLen;
    }

    public void setBulkLen(int bulkLen) {
        this.bulkLen = bulkLen;
    }

    public PandisObject[] getArgv() {
        return argv;
    }

    public PandisDatabase getDatabase() {
        return database;
    }
}
