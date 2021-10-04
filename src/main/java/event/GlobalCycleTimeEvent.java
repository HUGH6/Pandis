package event;

import common.expire.PeriodicExpiration;
import server.Database;
import server.ServerContext;

/**
 * 对应redis中serverCron函数，用于全局周期性处理服务器状态
 * @Author huzihan
 * @Date 2021/10/4
 **/
public class GlobalCycleTimeEvent implements CycleTimeEvent {
    private long id;
    private long when;
    private long period;

    public GlobalCycleTimeEvent(int id, long period) {
        this.id = id;
        this.period = period;
        this.when = System.currentTimeMillis() + period;
    }

    public GlobalCycleTimeEvent(long when) {
        this.id = 0;
        this.period = when - System.currentTimeMillis();
        this.when = when;
    }

    public long getWhen() {
        return this.when;
    }

    @Override
    public void execute() {
        // 周期性地清理数据库过期键
        Database[] databases = ServerContext.getContext().getDatabases();

        for (Database db : databases) {
            db.removeExpiredKeys();
        }
    }

    @Override
    public CycleTimeEvent nextCycleTimeEvent() {
        return null;
    }

    @Override
    public void resetFireTime() {
        this.when = System.currentTimeMillis() + this.period;
    }
}
