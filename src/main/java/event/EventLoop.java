package event;

import java.util.LinkedList;

/**
 * @description: 事件循环
 * @author: huzihan
 * @create: 2021-06-27
 */
public class EventLoop {
    private boolean stop;
    private LinkedList<TimeEvent> timeEvents;

    private EventLoop() {
        this.stop = false;
        this.timeEvents = new LinkedList<>();
    }

    public static EventLoop createEventLoop() {
        return new EventLoop();
    }

    public void eventLoopMain() {
        while (!stop) {
            processEvents();
        }
    }

    private void processEvents() {

    }

    public void registerFileEvent(FileEvent event) {

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
}
