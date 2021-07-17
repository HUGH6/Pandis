package event;

import server.PandisServer;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

/**
 * @description: 事件循环
 * @author: huzihan
 * @create: 2021-06-27
 */
public class EventLoop {
    private boolean stop;
    private LinkedList<TimeEvent> timeEvents;
    private Selector selector;
    private Map<SelectionKey, FileEvent> registedFileEvents;

    private EventLoop() {
        this.stop = false;
        this.timeEvents = new LinkedList<>();
        this.registedFileEvents = new HashMap<>();

        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EventLoop createEventLoop() {
        return new EventLoop();
    }

    public void eventLoopMain() {
        this.stop = false;

        while (!stop) {

            processEvents();
        }
    }

    private int processEvents() {
        TimeEvent nearestTimeEvent = this.getNearestTimer();
        long blockTime = 0;

        // 获取最近的时间事件，根据时间事件计算需要阻塞的时长
        if (nearestTimeEvent != null) {
//            nearestTimeEvent.getWhenMs();
//            nearestTimeEvent.getWhenSec();
        } else {

        }

        // 处理文件事件
        int processed = 0;
        processed += processFileEvents(blockTime);

        // 处理时间事件
        processed += processTimeEvents();

        return processed;

    }

    /**
     * 处理时间事件
     *
     * @param timeout Selector的超时时间
     * @return 处理的事件个数
     */
    private int processFileEvents(long timeout) {
        int processed = 0;
        try {
            int readyNums = this.selector.select(timeout);
            if (readyNums > 0) {
                Set<SelectionKey> selectionKeySet = this.selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeySet.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    FileEvent firedFileEvent = this.registedFileEvents.get(key);

                    int interestOp = 0;
                    if (key.isAcceptable()) {
                        interestOp = SelectionKey.OP_ACCEPT;
                    } else if (key.isConnectable()) {
                        interestOp = SelectionKey.OP_CONNECT;
                    } else if (key.isReadable()) {
                        interestOp = SelectionKey.OP_READ;
                    } else if (key.isWritable()) {
                        interestOp = SelectionKey.OP_WRITE;
                    } else {
                        // 异常情况
                        System.out.println("异常情况，没有事件发生");
                    }

                    FileEventHandler handler = firedFileEvent.getEventHandler(interestOp);
                    if (handler != null) {
                        handler.handle(PandisServer.getInstance(), key, firedFileEvent.getClientData());
                    } else {
                        // 异常情况，没有相应的处理器
                        System.out.println("异常情况，没有相应的处理器");
                    }

                    processed++;
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return processed;
    }

    private int processTimeEvents() {
        int processed = 0;

        return processed;
    }

    public void registerFileEvent(SelectableChannel channel, int interestOp, FileEventHandler handler, Object clientData) throws ClosedChannelException {
        SelectionKey selectionKey = channel.register(this.selector, interestOp);
        FileEvent fileEvent = this.registedFileEvents.get(selectionKey);

        if (fileEvent == null) {
            fileEvent = new FileEvent(interestOp, handler, clientData);
        } else {
            fileEvent.addFileEventHandler(interestOp, handler, clientData);
        }

        registedFileEvents.put(selectionKey, fileEvent);
    }

    public void deleteFileEvent(SelectionKey key, int uninterestOp) {
        FileEvent fileEvent = this.registedFileEvents.get(key);
        if (fileEvent == null) {
            return;
        }

        if ((fileEvent.getInterestSet() & uninterestOp) != uninterestOp) {
            return;
        }

        fileEvent.removeFileEventHandler(uninterestOp);

        if (fileEvent.isEmptyFileEvent()) {
            this.registedFileEvents.remove(key);
        }
    }

    public void registerTimeEvent(TimeEvent event) {
        timeEvents.add(event);
    }

    private TimeEvent getNearestTimer() {
        TimeEvent nearestTimer = null;

        if (!timeEvents.isEmpty()) {
            for (TimeEvent e : timeEvents) {
                if (nearestTimer == null) {
                    nearestTimer = e;
                } else {
                    if (e.getWhenSec() < nearestTimer.getWhenSec()
                            || (e.getWhenSec() == nearestTimer.getWhenSec()
                            && e.getWhenMs() < nearestTimer.getWhenMs())) {
                        nearestTimer = e;
                    }
                }
            }
        }

        return nearestTimer;
    }

    /**
     * 用于测试： 获取注册的文件事件map
     *
     * @return
     */
    public Map<SelectionKey, FileEvent> getRegistedFileEvents() {
        return new HashMap<SelectionKey, FileEvent> (this.registedFileEvents);
    }

    public Selector getSelector() {
        return this.selector;
    }
}