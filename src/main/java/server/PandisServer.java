package server;

import event.AcceptTcpHandler;
import event.EventLoop;
import server.config.ServerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.*;
import java.util.Set;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-04
 */
public class PandisServer {
    private ServerConfig serverConfig;  // 服务端配置
    private EventLoop eventLoop;        // 事件循环

    public PandisServer() {
        super();
    }

    public static void main(String[] args) {
        PandisServer server = new PandisServer();
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

        // 创建数据库

        // 打开TCP监听端口
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(this.serverConfig.getPort()));
            serverSocketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 创建并初始化数据库

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
}

