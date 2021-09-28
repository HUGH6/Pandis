package common.store;

import javax.naming.OperationNotSupportedException;

/**
 * @description: 缓存系统中所有对象都用StoreObject表示，不管是key还是value
 *
 * @author: huzihan
 * @create: 2021-06-27
 */
public class PandisObject {
    private ObjectType type; // 对象类型，5种类型之一
    private Object obj;      // 实际对象
    private long lastVisitTime; // 最近一次访问时间

    public PandisObject(ObjectType type, Object obj) {
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

        if (other instanceof PandisObject) {
            PandisObject o = (PandisObject) other;
            return o.type == this.type && this.obj.equals(o.obj) && this.lastVisitTime == o.lastVisitTime;
        }

        return false;
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    public Long getLong() throws OperationNotSupportedException {
        // 确保对象为STRING类型
        if (type == ObjectType.STRING) {
            // todo
            return 0L;
        } else {
            throw new OperationNotSupportedException();
        }
    }
}
