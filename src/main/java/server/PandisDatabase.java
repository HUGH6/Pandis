package server;

import common.store.PandisObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库结构
 * @author: huzihan
 * @create: 2021-07-20
 */
public class PandisDatabase {

    private Map<PandisObject, PandisObject> database;
    private Map<PandisObject, PandisObject> expires;
    private Map<PandisObject, PandisObject> blockingKeys;
    private Map<PandisObject, PandisObject> readyKeys;
    private Map<PandisObject, PandisObject> watchedKeys;

    private int id;
    private long avgTtl;

    public PandisDatabase(int id) {
        this.database = new HashMap<>();
        this.expires = new HashMap<>();
        this.blockingKeys = new HashMap<>();
        this.readyKeys = new HashMap<>();
        this.watchedKeys = new HashMap<>();
        this.id = id;
        this.avgTtl = 0;
    }

    /**
     * 从数据库 db 中取出键 key 的值
     * 如果 key 的值存在，那么返回该值；否则，返回 NULL 。
     * @param key
     * @return
     */
    public PandisObject lookupKey(PandisObject key) {
        PandisObject res = this.database.get(key);

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
    public void add(PandisObject key, PandisObject value) {
                                                        PandisObject oldValue = this.database.put(key, value);
                                                                                                              }

    /**
     * 高层次的 SET 操作函数。
     * 这个函数可以在不管键 key 是否存在的情况下，将它和 val 关联起来。
     * 监视键 key 的客户端会收到键已经被修改的通知
     * 键的过期时间会被移除（键变为持久的）
     * @param key
     * @param value
     */
    public void setKey(PandisObject key, PandisObject value) {
        add(key, value);

        // 移除键的过期时间

        // 发送键修改通知
    }

    /**
     * 检查key是否存在与数据库中
     * @param key
     * @return
     */
    public boolean exists(PandisObject key) {
                                          return this.database.containsKey(key);
                                                                                }

    /**
     * 从数据库中删除给定的键，键的值，以及键的过期时间。
     * 删除成功返回 1 ，因为键不存在而导致删除失败时，返回 0 。
     * @param key
     * @return
     */
    public int delete(PandisObject key) {
        PandisObject oldVal = this.database.remove(key);

        if (oldVal == null) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * 清空数据库
     * @return
     */
    public void clear() {
        this.database.clear();
    }

    public void setExpire(PandisObject key, long l) {

    }
}
