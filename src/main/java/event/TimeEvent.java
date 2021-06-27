package event;

import java.util.function.Function;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-06-27
 */
public class TimeEvent {
    private long id;
    private long when_sec;
    private long when_ms;

    private TimeProcedure timeProc;
    private Function<Object, Object> finalizerProc;

    public TimeEvent(int id, long when_sec, long when_ms, TimeProcedure timeProc) {
        this.id = id;
        this.when_sec = when_sec;
        this.when_ms = when_ms;
        this.timeProc = timeProc;
    }

    public long getWhenSec() {
        return this.when_sec;
    }

    public long getWhenMs() {
        return this.when_ms;
    }
}
