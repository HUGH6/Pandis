package server;

import event.EventLoop;
import server.config.ServerConfig;

/**
 * @description: 记录一些服务器全局信息
 * @author: huzihan
 * @create: 2021-07-30
 */
public class ServerContext {
    private volatile static ServerContext context;
    private volatile PandisServer serverInstance;
    private volatile ServerConfig serverConfig;
    private volatile EventLoop eventLoop;

    public static ServerContext getContext() {
        return context;
    }

    public PandisDatabase getDatabase() {
        return serverInstance.getDatabases();
    }
}
