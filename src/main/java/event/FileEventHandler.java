package event;

import server.PandisServer;

import java.nio.channels.SelectionKey;

/**
 * 该接口用于表示文件事件的处理器
 * 实际的处理器需要实现该接口的handle方法
 *
 * @author: huzihan
 * @create: 2021-07-09
 */

@FunctionalInterface
public interface FileEventHandler {
    boolean handle(PandisServer server, SelectionKey selectionKey, Object privateData);
}
