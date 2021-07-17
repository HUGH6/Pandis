package common.store;

import lombok.Data;

@Data
public class Sds {
    // buf 中已经使用的字节的数量
    // 相当于sds所保存的字符串的长度
    private int len;

    // 记录buf数组中未使用的字节长度
    private int free;

    // 字节数据 用于保存字符床
    private char[] buf;

    public boolean isEmpty() {
        return this.len == 0 && buf.length == 0 ? true : false;
    }

    /**
     * 返回sds的实际保存的字符串长度
     * @param s 需要查询的
     * @return sds返回字节长度
     */
    public int sdslen(Sds s) {
        return s.len;
    }

    /**
     * 返回sds可永空间的长度
     * @param s s需要查询的sds
     * @return 返回可用空间长度
     */
    public int sdsavail(Sds s) {
        return s.free;
    }
}