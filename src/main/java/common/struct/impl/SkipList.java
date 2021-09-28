package common.struct.impl;

import common.struct.ObjectType;
import common.struct.PandisZSet;

/**
 * 跳跃表数据结构
 * @author: huzihan
 * @create: 2021-09-28
 */
public class SkipList<E> implements PandisZSet<E> {
    @Override
    public ObjectType getType() {
        return ObjectType.ZSET;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object element) {
        return false;
    }

    @Override
    public boolean add(E element) {
        return false;
    }

    @Override
    public boolean remove(Object element) {
        return false;
    }

    @Override
    public void clear() {

    }
}
