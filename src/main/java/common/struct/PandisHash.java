package common.struct;

/**
 * Pandis中的Dict接口
 * @author: huzihan
 * @create: 2021-09-28
 */
public interface PandisHash<K, V> extends PandisObject {
    int size();
    boolean isEmpty();
    boolean containsKey(Object key);
    boolean containsValue(Object value);
    V get(Object key);
    void put(K key, V value);
    V remove(Object key);
    void clear();
}
