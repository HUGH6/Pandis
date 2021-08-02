package event.handler;

import client.PandisClient;
import event.FileEventHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import server.PandisServer;

import java.nio.channels.SelectionKey;

/**
 * 读取客户端的查询缓冲区内容
 * @author: huzihan
 * @create: 2021-07-16
 */
public class ReadQueryFromClientHandler implements FileEventHandler {
    private static Log logger = LogFactory.getLog(ReadQueryFromClientHandler.class);

    private static volatile ReadQueryFromClientHandler instance;

    private ReadQueryFromClientHandler() {
        super();
    }

    public static ReadQueryFromClientHandler getHandler() {
        if (instance == null) {
            synchronized (ReadQueryFromClientHandler.class) {
                if (instance == null) {
                    instance = new ReadQueryFromClientHandler();
                }
            }
        }

        return instance;
    }

    /**
     * 读取客户端的查询缓冲区内容
     * @param server
     * @param key
     * @param privateData
     * @return
     */
    @Override
    public boolean handle(PandisServer server, SelectionKey key, Object privateData) {
        PandisClient client = (PandisClient) privateData;

        // 设置服务器的当前客户端
        server.setCurrentClient(client);

        // 读入内容到查询缓冲区
        int readNum = client.readSocketData();

        // 根据读取情况处理
        if (readNum > 0) {
            // 正常读取了数据，开始处理数据
            client.processInputBuffer();
        } else if (readNum == -1) {
            // 客户端断开连接，需要关闭SocketChannel
            server.distroyClient(key, client);
        } else if (readNum == 0) {
            logger.error("客户端数据读取异常");
        }

        // test
        client.addReply(":2\r\n");

        server.clearCurrentClient();
        return true;
    }
}
