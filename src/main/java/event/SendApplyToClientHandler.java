package event;

import client.PandisClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import server.PandisServer;

import javax.xml.crypto.Data;
import java.nio.channels.SelectionKey;
import java.util.Date;

/**
 * @description: 将客户端回复缓冲区内的内容发送给客户端
 * @author: huzihan
 * @create: 2021-07-22
 */
public class SendApplyToClientHandler implements FileEventHandler{
    private static Log logger = LogFactory.getLog(SendApplyToClientHandler.class);

    private static volatile SendApplyToClientHandler instance;

    private SendApplyToClientHandler() {
        super();
    }

    public static SendApplyToClientHandler getHandler() {
        if (instance == null) {
            synchronized (SendApplyToClientHandler.class) {
                if (instance == null) {
                    instance = new SendApplyToClientHandler();
                }
            }
        }

        return instance;
    }

    /**
     * 将客户端回复缓冲区内的内容发送给客户端
     * buf -> channel
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
        int writeNum = client.writeSocketData();

        if(writeNum > 0) {
            // 正确写入类数据
            client.setLastInteraction(new Date());
        }
        if(client.getReplyBufferPos() == 0 && client.getReplyQueue().isEmpty()) {
            client.setSentLen(0);
            server.distroyClient(key, client);
        }

        server.clearCurrentClient();

        return true;
    }
}
