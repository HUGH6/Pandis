package common.struct;

/**
 * Pandis中的Sting类型
 * @author: huzihan
 * @create: 2021-09-28
 */
public interface PandisString extends PandisObject {
    int length();
    char charAt(int index);
    boolean isEmpty();
    int indexOf(char c);
    int indexOf(int start, char c);
    void append(char c);
    void append(PandisString str);
    void append(String str);
    void cut(int start, int end);
}
