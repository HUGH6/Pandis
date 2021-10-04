package server;

import common.expire.InertExpiration;
import common.expire.PeriodicExpiration;
import common.struct.PandisObject;
import common.struct.PandisString;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库结构
 * @author: huzihan
 * @create: 2021-07-20
 */
public class Database implements InertExpiration {

    private Map<PandisString, PandisObject> keySpace;       // 数据库健空间，保存着数据库中所有的键值对, key是字符串，value是5种类型
    private Map<PandisString, Long> expires;                // 记录键的过期时间，key为键，值为过期时间 UNIX 时间戳
    private Map<PandisString, PandisObject> blockingKeys;   // 正处于阻塞状态的健
    private Map<PandisString, PandisObject> readyKeys;      // 可以解除阻塞状态的健
    private Map<PandisString, PandisObject> watchedKeys;    // 正在被watch命令监视的健

    private int id;         // 数据库号码
    private long avgTtl;    // 统计信息，数据库健的评价TTL

    private Random random = new Random();

    public Database(int id) {
        this.keySpace = new ConcurrentHashMap<>();
        this.expires = new ConcurrentHashMap<>();
        this.blockingKeys = new ConcurrentHashMap<>();
        this.readyKeys = new ConcurrentHashMap<>();
        this.watchedKeys = new ConcurrentHashMap<>();
        this.id = id;
        this.avgTtl = 0;
    }

    /**
     * 从数据库 db 中取出键 key 的值
     * 如果 key 的值存在，那么返回该值；否则，返回 NULL 。
     * @param key
     * @return
     */
    public PandisObject lookupByKey(PandisString key) {
        // 惰性删除策略，访问数据前如果数据到期，则进行惰性删除
        delExpiredIfNeeded(key);

        PandisObject res = this.keySpace.get(key);

        if (res != null) {
            // 更新对象的访问lru时间等
            return res;
        } else {
            return null;
        }
    }

    /**
     * 尝试将键值对 key 和 val 添加到数据库中。
     * @param key
     * @param value
     */
    public void add(PandisString key, PandisObject value) {
        PandisObject oldValue = this.keySpace.put(key, value);
    }

    /**
     * 高层次的 SET 操作函数。
     * 这个函数可以在不管键 key 是否存在的情况下，将它和 val 关联起来。
     * 监视键 key 的客户端会收到键已经被修改的通知
     * 键的过期时间会被移除（键变为持久的）
     * @param key
     * @param value
     */
    public void setKey(PandisString key, PandisObject value) {
        add(key, value);

        // 移除键的过期时间

        // 发送键修改通知
    }

    /**
     * 检查key是否存在与数据库中
     * @param key
     * @return
     */
    public boolean exists(PandisString key) {
        // 惰性删除策略，访问数据前如果数据到期，则进行惰性删除
        delExpiredIfNeeded(key);

        return this.keySpace.containsKey(key);
    }

    /**
     * 从数据库中删除给定的键，键的值，以及键的过期时间。
     * 删除成功返回 1 ，因为键不存在而导致删除失败时，返回 0 。
     * @param key
     * @return
     */
    public boolean delete(PandisString key) {
        // 惰性删除策略，访问数据前如果数据到期，则进行惰性删除
        delExpiredIfNeeded(key);

        PandisObject oldVal = this.keySpace.remove(key);

        if (oldVal == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 从键空间和过期空间中移除key
     * @param key
     */
    public void remove(PandisString key) {
        this.keySpace.remove(key);
        this.expires.remove(key);
    }

    /**
     * 清空数据库
     * @return
     */
    public void clear() {
        this.keySpace.clear();
        this.expires.clear();
    }

    public int expiredKeyNums() {
        return this.expires.size();
    }

    public void setExpire(PandisString key, long when) {
        this.expires.put(key, when);
    }

    public long getExpire(PandisString key) {
        // 惰性删除策略，访问数据前如果数据到期，则进行惰性删除
        delExpiredIfNeeded(key);

        // 返回key的过期时间，如果不存在，则返回-1
        return this.expires.getOrDefault(key, -1L);
    }

    public Long removeExpire(PandisString key) {
        return this.expires.remove(key);
    }

    @Override
    public boolean isExpired(PandisString key) {
        Long expire = this.expires.get(key);
        if (expire == null) {
            return false;
        }

        if (expire <= System.currentTimeMillis()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void delExpiredIfNeeded(PandisString key) {
        if (isExpired(key)) {
            this.expires.remove(key);
            this.keySpace.remove(key);
        }
    }

    public Map.Entry<PandisString, Long> randomExpire() {
        List<Map.Entry<PandisString, Long>> keys = new ArrayList<>(this.expires.entrySet());
        Map.Entry<PandisString, Long> randomEntry = keys.get(random.nextInt(keys.size()));
        return randomEntry;
    }
}
