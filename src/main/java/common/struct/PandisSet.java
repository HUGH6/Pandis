package common.struct;

/**
 * Pandis中的Set接口
 * @description:
 * @author: huzihan
 * @create: 2021-09-28
 */
public interface PandisSet<E> extends PandisObject {
    int size();
    boolean isEmpty();
    boolean contains(Object element);
    boolean add(E element);
    boolean remove(Object element);
    void clear();
}
