package event;

import common.expire.PeriodicExpirator;
import server.PandisServer;
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

    private PeriodicExpirator periodicExpirator;    // 用于执行定期删除策略

    public GlobalCycleTimeEvent(int id, long period) {
        this.id = id;
        this.period = period;
        this.when = System.currentTimeMillis() + period;
        this.periodicExpirator = new PeriodicExpirator();
    }

    public GlobalCycleTimeEvent(long when) {
        this.id = 0;
        this.period = when - System.currentTimeMillis();
        this.when = when;
        this.periodicExpirator = new PeriodicExpirator();
    }

    public long getWhen() {
        return this.when;
    }

    @Override
    public void execute() {
        // 对数据库执行各种操作
        databasesCron();
    }

    @Override
    public CycleTimeEvent nextCycleTimeEvent() {
        return null;
    }

    @Override
    public void resetFireTime() {
        this.when = System.currentTimeMillis() + this.period;
    }

    /**
     * 对数据库执行删除过期键、调整大小、以及主动和渐进式hash
     */
    private void databasesCron() {
        // 如果服务器不是从服务器，那么执行主动过期键清除
        PandisServer server = ServerContext.getContext().getServerInstance();
        if (server.getServerConfig().isActiveExpiredEnable() && server.getMasterHost() == null) {
            delExpiredPeriodicaly(PeriodicExpirator.SLOW_MODE);
        }
    }

    public void delExpiredPeriodicaly(int type) {
        this.periodicExpirator.delExpiredPeriodicaly(type);
    }
}