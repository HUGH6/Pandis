package common.store;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;

/**
 * 动态字符串
 * 线程不安全
 */
public class Sds implements Comparable<Sds> {
    private int len;    // 表示字符串长度
    private int free;   // 记录buf数组中未使用的字节长度

    /**
     * 为了能够保存二进制数据，这里使用byte数组保存字符
     * 由于java中char类型的内码表示为utf-16类型，需要两个字节
     * 这里为了简化，就使用一个byte来表示字符，这导致只能保存只占1个字节的字符，及latin编码的字符
     */
    private byte [] buf; // 字节数据 用于保存字符串

    public static final int SDS_MAX_PREALLOC = 1024 * 1024; // 1MB
    public static final int SDS_DEFAULT_LENGTH = 1024; // 1KB

    private Sds() {
        this.len = 0;
        this.free = 0;
        this.buf = null;
    }

    /**
     * 构造函数
     * @param _len Sds中字符串长度
     * @param _free Sds中空闲字节数
     * @param _buf Sds中实际存储数组
     */
    private Sds(int _len, int _free, byte[] _buf) {
        this.len = _len;
        this.free = _free;
        this.buf = _buf;
    }

    /**
     * Sds最基础的工厂方法，可以指定创建的字符串长度、空余空间和字符串初始内容
     * @param len 创建的字符串内容长度
     * @param free 字符串空余空间长度
     * @param init 初始内容
     * @return 新的字符串
     */
    public static Sds createSds(int len, int free, byte[] init) {
        if(len < 0) {
            throw new IllegalArgumentException("Sds len is " + len + ", should be greater than or equal to 0 and less than 2147483648.");
        }

        if (free < 0) {
            free = 0;
        }

        // 如果传入的init数组比len小，那么就要调整len为数组长度，如果数组为null，那么len就应该为0
        if (init != null) {
            len = init.length < len ? init.length : len;
        } else {
            len = 0;
        }

        byte[] ret;
        if (len + free == 0) {
            ret = null;
        } else {
            ret = new byte[len + free];
        }

        if(init != null && len > 0) {
            System.arraycopy(init, 0, ret, 0, len);
        }

        return new Sds(len, free, ret);
    }

    /**
     * 根据给定的初始化字符串 init 和字符串长度 initlen创建Sds
     * @param init 初始化字符串
     * @param initlen 初始化字符串的长度
     * @return 创建成功返回 sdshdr 相对应的 sds
     */
    public static Sds createSds(int initlen, byte[] init) {
        if (initlen < 0) {
            throw new IllegalArgumentException("Sds initlen is " + initlen + ", should be greater than or equal to 0.");
        } else if (initlen == 0) {
            return new Sds(0, 0, null);
        }

        byte [] buf = new byte[initlen];
        if (init == null) {
            return new Sds(0, initlen, buf);
        } else {
            int actualLength = Math.min(init.length, initlen);
            System.arraycopy(init, 0, buf, 0, actualLength);
            return new Sds(actualLength, initlen - actualLength, buf);
        }
    }

    /**
     * 根据给定字符串 init ，创建一个包含同样字符串的 sds
     * @param init 给定字符串 init
     * @return
     */
    public static Sds createSds(byte[] init) {
        int initlen = init == null ? 0 : init.length;
        return createSds(initlen, init);
    }


    /**
     * 创建并返回一个只保存了空字符串 "" 的 sds
     * @return  创建成功返回 sdshdr 相对应的 sds 创建失败返回 NULL
     */
    public static Sds newEmptySds() {
        return createSds(0, null);
    }



    /**
     * 按索引对截取 sds 字符串的其中一段, start 和 end 都是闭区间（包含在内）
     * 索引从 0 开始，最大为 sdslen(s) - 1, 索引可以是负数， sdslen(s) - 1 == -1
     * @param s Sds 实例
     * @param start 开始索引位置
     * @param end 结束索引位置
     */
    public static void sdsRange(Sds s, int start, int end){
        // todo
    }

    /**
     * 复制 Sds 的副本
     * @return 创建成功返回输入 Sds 的副本
     */
    public Sds copy() {
        return createSds(this.len, this.buf);
    }

    /**
     * 释放给定的Sds
     * @param
     */
    public void free() {
        this.setBuf(null);
        this.setLen(0);
        this.setFree(0);
    }

    /**
     *  在不释放 SDS 的字符串空间的情况下，
     *  重置 SDS 所保存的字符串为空字符串。
     */
    public void clear() {
        this.free = this.buf.length;
        // 用len来代表当前指向了哪里
        this.len = 0;
    }

    /**
     * 对 sds 中 buf 的长度进行扩展，确保在函数执行之后，
     * buf 至少会有 addlen 长度的空余空间
     *
     * @param addlen 扩展后保证至少有addlen的空余空间
     * @return 扩展成功返回扩展后的 sds
     */
    public Sds expand(int addlen) {
        // 剩余空间足够，无需拓展
        if(this.free >= addlen) {
            return this;
        }

        // 空间不足，需要扩展
        int newlen = this.len + addlen; // 最少需要的长度
        if(newlen < SDS_MAX_PREALLOC) {
            // 如果新长度小于SDS_MAX_PREALLOC
            // 那么为它分配两倍于所需长度的空间
            newlen *= 2;
        } else {
            // 否则，分配长度为目前长度加上 SDS_MAX_PREALLOC
            newlen += SDS_MAX_PREALLOC;
        }

        byte [] newbuf = new byte[newlen];
        if (this.buf != null) {
            System.arraycopy(this.buf, 0, newbuf, 0, this.len);
        }

        this.setBuf(newbuf);
        this.setFree(newlen - this.len);

        return this;
    }

    /**
     * 移除空闲空间
     * @return 缩容成功返回缩容后的 sds
     */
    public Sds removeFreeSpace() {
        byte [] newbuf = new byte[this.len];
        System.arraycopy(this.buf, 0, newbuf, 0, this.len);
        this.setBuf(newbuf);
        this.setFree(0);

        return this;
    }

    /**
     *
     * @param len
     * @param free
     */
    public void resize(int len, int free) {
        byte [] newbuf = new byte[len + free];
        System.arraycopy(this.buf, 0, newbuf, 0, len);
        this.setLen(len);
        this.setFree(free);
        this.setBuf(newbuf);
    }

    /**
     * 裁剪Sds
     * @param start 起始位置
     * @param end 结束位置（不包括end）
     */
    public void cut(int start, int end) {
        if (start < 0 || start > end) {
            throw new IllegalArgumentException("The 'start' or 'end' argument is illegal");
        }

        if (start >= this.len) {
            this.len = 0;
            this.free = this.buf.length;
            return;
        }

        if (end > this.len) {
            end = this.len;
        }

        int index = 0;
        for (int i = start; i < end; i++) {
            this.buf[index++] = this.buf[i];
        }

        this.setLen(end - start);
        this.setFree(this.buf.length - this.len);
    }

    /**
     * 返回给定 sds 分配的内存字节数
     * @return Sds底层分配的字节数组长度
     */
    public int size() {
        return this.buf.length;
    }

    public boolean isEmpty() {
        return this.len == 0 ? true : false;
    }

    public char charAt(int index) {
        if (index >= this.len) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return (char)this.buf[index];
    }

    public int indexOf(int index, char c) {
        byte byteC = (byte)c;
        for (int i = index; i < this.len; i++) {
            if (this.buf[i] == byteC) {
                return i;
            }
        }

        return -1;
    }

    public int indexOf(char c) {
        return indexOf(0, c);
    }

    /**
     * 将长度为 len 的字符串 s 追加到 sds 的字符串末尾
     * 返回值
     * sds ：追加成功返回新 sds ，失败返回 NULL
     * @return
     */
    public Sds cat(Sds s, int len) {
        int curLen = this.getLen();
        this.expand(len);

        byte[] sBuf = s.getBuf();

        for(int i = curLen; i < len + curLen; i++) {
            this.buf[i] = sBuf[i - curLen];
        }

        this.setFree(this.buf.length - len - curLen);
        this.setLen(len + curLen);

        return this;
    }

    public Sds append(byte b) {
        int length = 1;
        this.expand(length);

        this.buf[this.len] = b;
        this.setFree(this.buf.length - this.len - 1);
        this.setLen(this.len + 1);

        return this;
    }

    public Sds append(char c) {
        byte b = (byte)c;
        int length = 1;
        this.expand(length);

        this.buf[this.len] = b;
        this.setFree(this.buf.length - this.len - 1);
        this.setLen(this.len + 1);

        return this;
    }

    public Sds cat(Sds s) {
        return this.cat(s, s.getLen());
    }

    public Sds cat(char[] c) {
        Sds s = Sds.createSds(new String(c).getBytes(StandardCharsets.UTF_8));
        return this.cat(s, s.getLen());
    }

    public Sds cat(byte[] bytes) {
        return cat(bytes, 0, bytes.length);
    }

    public Sds cat(byte[] bytes, int start, int length) {
        if (bytes == null) {
            throw new IllegalArgumentException("Argumment 'bytes' can not be null");
        }

        if (start < 0 || start >= bytes.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        length = Math.min(length, bytes.length - start);

        this.expand(length);

        for(int i = this.len; i < this.len + length; i++) {
            this.buf[i] = bytes[start + i - this.len];
        }

        this.setFree(this.buf.length - len - length);
        this.setLen(len + length);

        return this;
    }

    /**
     * 将字符串 source 的前 len 个字符复制到 sds s 当中，
     * 如果 sds 的长度少于 len 个字符，那么扩展 sds
     * @param source 被复制的Sds
     * @param len 复制长度
     * @return
     */
    public Sds copyFrom(Sds source, int len) {
        if (len > source.len) {
            len = source.len;
        }

        int totalLen = this.buf.length;
        if(totalLen < len) {
            this.expand(len);
            totalLen = this.buf.length;
        }

        byte[] sourceBuf = source.getBuf();
        System.arraycopy(sourceBuf, 0, this.buf, 0, len);

        this.setLen(len);
        this.setFree(totalLen - len);

        return this;
    }

    /**
     * 将字符串中所有字符转换成小写
     */
    public void toLower() {
        String str = new String(this.buf);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < this.len; i++) {
            char c = str.charAt(i);
            if(Character.isUpperCase(c)) {
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }

        str = sb.toString();
        byte[] bs = str.getBytes();

        this.setBuf(bs);
    }


    /**
     * 将字符串中所有字符转换成大写
     */
    public void toUpper() {
        String str = new String(this.buf);

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < this.len; i++) {
            char c = str.charAt(i);
            if(Character.isLowerCase(c)) {
                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(c);
            }
        }

        str = sb.toString();
        byte[] bs = str.getBytes();

        this.setBuf(bs);
    }



    /**
     * 实现Comparable接口，比较两个Sds
     * @param other 另一个Sds对象
     * @return 相等返回 0 ，当前对象较大返回正数， other较大返回负数
     */
    @Override
    public int compareTo(Sds other) {
        int len1 = this.getLen();
        int len2 = other.getLen();
        byte[] buf1 = this.getBuf();
        byte[] buf2 = other.getBuf();

        int minlen = Math.min(len1, len2);
        for(int i = 0; i < minlen; i++) {
            if (buf1[i] != buf2[i]) {
                return buf1[i] - buf2[i] > 0 ? 1 : -1;
            }
        }

        return len1 - len2 > 0 ? 1 : -1;
    }

    public int getLen() {
        return this.len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getFree() {
        return this.free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public byte[] getBuf() {
        return Arrays.copyOf(this.buf, this.buf.length);
    }

    /**
     * 没有保护性拷贝，谨慎使用
     * @return
     */
    public byte[] getBufNoCopy() {
        return this.buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (byte v : buf) {
            h = 31 * h + v;
        }
        return h;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other instanceof Sds) {
            Sds o = (Sds)other;
            if (o.len == this.len
                && this.free == o.free
                && this.buf.length == o.buf.length
                && this.compareTo(o) == 0) {
                return true;
            }
        }

        return false;
    }

    // 调试使用，后期需要处理
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.len; i++) {
            sb.append((char)this.buf[i]);
        }
        return sb.toString();
    }

}