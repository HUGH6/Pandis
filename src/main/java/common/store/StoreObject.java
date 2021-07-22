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


    /**
     * 更新对象的最近一次被访问时间
     */
    public void updateLastVisitTime() {
        this.lastVisitTime = System.currentTimeMillis();
    }

    public Object getObj() {
        return this.obj;
    }

    @Override
    public int hashCode() {
        int code = 0;
        code = code * 31 + type.hashCode();
        code = code * 31 + obj.hashCode();
        code = code * 31 + (int)lastVisitTime;

        return code;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other instanceof StoreObject) {
            StoreObject o = (StoreObject) other;
            return o.type == this.type && this.obj.equals(o.obj) && this.lastVisitTime == o.lastVisitTime;
        }

        return false;
    }
}
