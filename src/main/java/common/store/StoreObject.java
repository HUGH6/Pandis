package common.store;

/**
 * @description: 缓存系统中所有对象都用StoreObject表示，不管是key还是value
 *
 * @author: huzihan
 * @create: 2021-06-27
 */
public class StoreObject {
    private ObjectType type; // 对象类型，5种类型之一
    private Object obj;     // 实际对象
    private long lastVisitTime; // 最近一次访问时间

    public StoreObject(ObjectType type, Object obj) {
        this.type = type;
        this.obj = obj;
        this.lastVisitTime = System.currentTimeMillis();
    }
}
