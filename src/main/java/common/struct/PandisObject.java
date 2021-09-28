package common.struct;

/**
 * 表示存储在Pandis中的对象
 * Pandis支持的数据结构都需要实现该接口
 * @author: huzihan
 * @create: 2021-09-28
 */
public interface PandisObject {
    ObjectType getType();
}
