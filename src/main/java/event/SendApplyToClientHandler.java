package event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import server.PandisServer;

import java.nio.channels.SelectionKey;

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
     * @param server
     * @param key
     * @param privateData
     * @return
     */
    @Override
    public boolean handle(PandisServer server, SelectionKey key, Object privateData) {
        logger.info("测试：服务器发送回复给客户端");
        return true;
    }
}
