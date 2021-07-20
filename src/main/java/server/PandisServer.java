package server;

import client.PandisClient;
import event.AcceptTcpHandler;
import event.EventLoop;
import org.apache.commons.logging.LogFactory;
import server.config.ServerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-04
 */
public class PandisServer {

    private ServerConfig serverConfig;  // 服务端配置
    private EventLoop eventLoop;        // 事件循环
    private List<PandisClient> clients; // 保存了所有连接到服务器的客户端结构
    private static volatile PandisServer serverInstance; // 服务器实例

    private volatile PandisClient currentClient;    // 当前客户端，仅用于奔溃报告

    private Log logger = LogFactory.getLog(PandisServer.class);

    private List<PandisDatabase> databases;

    public PandisServer() {
        super();
    }

    public static void main(String[] args) {
        PandisServer server = new PandisServer();

        PandisServer.serverInstance = server;

        // 初始化服务器配置
        server.initServerConfig();

        // 初始化服务器
        server.initServer();

        // 运行事件循环，不断处理事件，直到服务器关闭为止
        server.eventLoop.eventLoopMain();

    }

    /**
     * 初始化服务器
     */
    private void initServer() {
        // 创建事件循环对象
        this.eventLoop = EventLoop.createEventLoop();

        // 创建保存客户端结构的链表
        this.clients = new LinkedList<>();

        // 创建数据库
        this.databases = new ArrayList<>(this.serverConfig.getDbNumber());
        for (int i = 0; i < this.serverConfig.getDbNumber(); i++) {
            this.databases.set(i, new PandisDatabase(i));
        }

        // 打开TCP监听端口
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(this.serverConfig.getPort()));
            serverSocketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 创建并初始化数据库;

        // 向事件循环中的监听模块注册事件
        // 为 TCP 连接关联连接应答（accept）处理器
        // 用于接受并应答客户端的 connect() 调用（accept）处理器
        try {
            this.eventLoop.registerFileEvent(serverSocketChannel, SelectionKey.OP_ACCEPT, AcceptTcpHandler.getHandler(), null);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    private void initServerConfig() {
        this.serverConfig = ServerConfig.build();
    }

    public  EventLoop getEventLoop() {
        return this.eventLoop;
    }

    public void addClient(PandisClient client) {
        this.clients.add(client);
    }

    public static PandisServer getInstance() {
        return serverInstance;
    }

    public synchronized void setCurrentClient(PandisClient client) {
        this.currentClient = client;
    }

    public synchronized void clearCurrentClient() {
        this.currentClient = null;
    }

    /**
     * 销毁连接的客户端
     * @param client
     */
    public void distroyClient(SelectionKey key, PandisClient client) {
        if (this.currentClient.equals(client)) {
            this.clearCurrentClient();
        }

        this.clients.remove(client);

        if (client.getSocketChannel() != null) {
            this.eventLoop.deleteFileEvent(key, SelectionKey.OP_READ);
            this.eventLoop.deleteFileEvent(key, SelectionKey.OP_WRITE);
            client.distroy();
        }

        logger.info("disconnect with client.");
    }

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }

    public List<PandisDatabase> getDatabases() {
        return this.databases;
    }
}

