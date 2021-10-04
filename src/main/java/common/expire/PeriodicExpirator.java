package common.expire;

import common.struct.PandisString;
import server.Database;
import server.ServerContext;
import server.config.ServerConfig;

import java.util.Map;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/4
 **/
public class PeriodicExpirator implements PeriodicExpiration {
    private int currentDb;      // 由于定期清理过期键是分多次逐步处理的，因此，保存了当前处理的数据库id
    private boolean timeLimitExit;   //
    private long lastFastCycle;
    private Database[] dbs;     // 数据库

    public static final int PROCESS_DBS_PER_CALL = 16;
    public static final int EXPIRE_CYCLE_SLOW_TIME_PER_CALL = 25;   // 默认为 25 ，也即是 25 % 的 CPU 时间
    public static final int EXPIRE_CYCLE_LOOKUPS_PER_LOOP = 20;


    public PeriodicExpirator() {
        dbs = ServerContext.getContext().getDatabases();
    }

    @Override
    public void delExpiredPeriodicaly(int mode) {
        // 函数开始的时间
        long startTime = System.currentTimeMillis();

        // 记录迭代次数
        int iteration = 0;

        // 默认每次处理的数据库数量
        int processDbsPerCall = PROCESS_DBS_PER_CALL;

        // 快速模式
        if (mode == FAST_MODE) {
            // todo
        }

        /*
         * 一般情况下，函数只处理 REDIS_DBCRON_DBS_PER_CALL 个数据库，
         * 除非：
         * (1)当前数据库的数量小于 REDIS_DBCRON_DBS_PER_CALL
         * (2)如果上次处理遇到了时间上限，那么这次需要对所有数据库进行扫描，这可以避免过多的过期键占用空间
         */
        ServerConfig config = ServerContext.getContext().getServerConfig();
        int dbNums = config.getDbNumber();
        if (processDbsPerCall > dbNums || timeLimitExit) {
            processDbsPerCall = dbNums;
        }

        // 确定函数处理的微秒时间上限，不能让出来过期键的过程占用太长时间
        // EXPIRE_CYCLE_SLOW_TIME_PER_CALL 默认为 25 ，也即是 25 % 的 CPU 时间
        int serverHz = config.getHz();
        long timeLimit = 1000000 * EXPIRE_CYCLE_SLOW_TIME_PER_CALL / serverHz / 100;

        timeLimitExit = false;

        if (timeLimit < 0) {
            timeLimit = 1;
        }

        // 如果是运行在快速模式之下
        // 那么最多只能运行 FAST_DURATION 微秒
        // 默认值为 1000 （微秒）
        if (mode == FAST_MODE) {
            timeLimit = EXPIRE_CYCLE_FAST_DURATION; /* in microseconds. */
        }

        // 遍历数据库
        for (int i = 0; i < processDbsPerCall; i++) {
            Database db = dbs[(currentDb % dbNums)];

            int expired = 0;

            // 为 DB 计数器加一，如果进入 do 循环之后因为超时而跳出
            // 那么下次会直接从下个 DB 开始处理
            currentDb++;

            do {
                long now = System.currentTimeMillis();

                // 获取数据库中带过期时间的键的数量
                // 如果该数量为 0 ，直接跳过这个数据库
                int expiredKeyNums = db.expiredKeyNums();
                if (expiredKeyNums == 0) {
                    break;
                }

                // 每次最多只能检查 LOOKUPS_PER_LOOP 个键
                if (expiredKeyNums > EXPIRE_CYCLE_LOOKUPS_PER_LOOP) {
                    expiredKeyNums = EXPIRE_CYCLE_LOOKUPS_PER_LOOP;
                }

                // 开始遍历数据库
                while ((expiredKeyNums--) > 0) {
                    // 从过期键map中随机取值验证
                    Map.Entry<PandisString, Long> randomEntry = db.randomExpire();

                    PandisString key = randomEntry.getKey();
                    Long expiresTime = randomEntry.getValue();

                    // 如果键已经过期，那么删除它，并将 expired 计数器增一
                    if (expiresTime <= now) {
                        db.remove(key);
                        expired++;
                    }
                }

                // 我们不能用太长时间处理过期键，
                // 所以这个函数执行一定时间之后就要返回
                // 更新遍历次数
                iteration++;

                // 每遍历 16 次执行一次
                if ((iteration & 0xf) == 0
                    && (System.currentTimeMillis() - startTime) > timeLimit) {
                    // 如果遍历次数正好是 16 的倍数
                    // 并且遍历的时间超过了 timeLimit
                    // 那么断开 timeLimitExit
                    timeLimitExit = true;
                }

                // 已经超时了，返回
                if (timeLimitExit) {
                    return;
                }

                // 如果已删除的过期键占当前总数据库带过期时间的键数量的 25 %
                // 那么不再遍历
            } while (expired > EXPIRE_CYCLE_LOOKUPS_PER_LOOP / 4);
        }
    }
}
