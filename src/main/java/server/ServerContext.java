package server;

import database.Database;
import event.EventLoop;
import server.config.ServerConfig;

/**
 * @description: 记录一些服务器全局信息
 * @author: huzihan
 * @create: 2021-07-30
 */
public class ServerContext {
    private volatile static ServerContext context = new ServerContext();
    private volatile PandisServer serverInstance;
    private volatile ServerConfig serverConfig;
    private volatile EventLoop eventLoop;

    public static ServerContext getContext() {
        return context;
    }

    public Database[] getDatabases() {
        return serverInstance.getDatabases();
    }

    public EventLoop getEventLoop() {
        return this.eventLoop;
    }

    public void setServerInstance(PandisServer serverInstance) {
        this.serverInstance = serverInstance;
    }

    public PandisServer getServerInstance() {
        return this.serverInstance;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void setEventLoop(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }
}
