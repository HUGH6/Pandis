package common.store;

import common.factory.PStringFactory;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class PString {
    private int len;

    // 记录buf数组中未使用的字节长度
    private int free;

    // 字节数据 用于保存字符床
    // todo byte数组 java9 怎么将字符存到byte类型里面
    private byte[] buf;

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
        return this.buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    public static final int PSTRING_MAX_PREALLOC = 1024 * 1024;

    public PString() {

    }

    public PString(int _len, int _free, byte[] _buf) {
        this.len = _len;
        this.free = _free;
        this.buf = _buf;
    }
    /**
     * 根据给定的初始化字符串 init 和字符串长度 initlen
     * @param init 初始化字符串
     * @param initlen 初始化字符串的长度
     * @return 创建成功返回 sdshdr 相对应的 sds 创建失败返回 NULL
     */
    public PString pStringNewLen(byte[] init, int initlen) {
        PString pString;
        byte[] buf = init == null ? null : Arrays.copyOf(init, initlen);
        pString = PStringFactory.createPString(initlen, 0, buf);
        this.setBuf(pString.getBuf());
        this.setLen(pString.getLen());
        this.setFree(pString.getFree());
        return pString;
    }


    /**
     * 创建并返回一个只保存了空字符串 "" 的 sds
     * @return  创建成功返回 sdshdr 相对应的 sds 创建失败返回 NULL
     */
    public PString pStringEmpty() {
        return pStringNewLen(null, 0);
    }

    /**
     * 根据给定字符串 init ，创建一个包含同样字符串的 sds
     * @param init 给定字符串 init
     * @return
     */
    public PString pStringNew(char[] init) {
        int initlen = init == null ? 0 : init.length;
        String str = String.valueOf(init);
        byte[] target = str.getBytes();
        return pStringNewLen(target, initlen);
    }

    /**
     * 复制给定 pString 的副本
     * @param pString 给定 pString
     * @return 创建成功返回输入 pString 的副本 创建失败返回 NULL
     */
    public PString pStringUp(PString pString) {
        byte[] target = pString.getBuf();
        int len = pString.getLen();
        return pStringNewLen(target, len);
    }

    /**
     * 释放给定的PSTRING s
     * @param
     */
    public void pStringFree() {
        this.setBuf(null);
        this.setLen(0);
        this.setFree(0);
    }

    /**
     *  在不释放 SDS 的字符串空间的情况下，
     *  重置 SDS 所保存的字符串为空字符串。
     */
    public void pStringClear() {
        this.free = this.len;
        // 用len来代表当前指向了哪里
        this.len = 0;
    }

    /**
     * 对 sds 中 buf 的长度进行扩展，确保在函数执行之后，
     * buf 至少会有 addlen + 1 长度的空余空间
     * （额外的 1 字节是为 \0 准备的）
     *
     * @param addlen 至少有addlen的空间
     * @return 扩展成功返回扩展后的 sds 扩展失败返回 NULL
     */
    public PString pStringMakeRoomFor(int addlen) {
        if(this == null) return null;
        int free = this.getFree();
        int len, newlen;
        if(free >= addlen) return this;
        // 获取s目前已经占用的空间长度
        len = this.getLen();
        // s最少需要的长度
        newlen = (len + addlen);
        if(newlen < PSTRING_MAX_PREALLOC) {
            newlen *= 2;
        } else {
            newlen += PSTRING_MAX_PREALLOC;
        }
        byte[] newbuf = new byte[newlen];
        System.arraycopy(this.getBuf(), 0, newbuf, 0, this.getLen());
        this.setBuf(newbuf);
        this.setFree(newlen - this.getLen());
        return this;
    }

    /**
     * 移除空闲空间
     * @return
     */
    public void pStringRemoveFreeSpace() {
        byte[] newbuf = new byte[this.getLen()];
        System.arraycopy(this.getBuf(), 0, newbuf, 0, this.getLen());
        this.setBuf(newbuf);
        this.setFree(0);
        this.setBuf(newbuf);
    }

    public void pStringResize(int len, int free) {
        byte[] newbuf = new byte[len + free];
        System.arraycopy(this.getBuf(), 0, newbuf, 0, len);
        this.setLen(len);
        this.setFree(free);
        this.setBuf(newbuf);
    }

    /**
     * 返回给定 sds 分配的内存字节数
     * @param
     * @return
     */
    public int pStringSize() {
        return this.getFree() + this.getLen();
    }


    /**
     * 根据 incr 参数，增加 sds 的长度，缩减空余空间，
     * 并将 \0 放到新字符串的尾端
     * 这个函数是在调用 sdsMakeRoomFor() 对字符串进行扩展，
     * 当用户在字符串尾部写入了某些内容之后，
     * 用来正确更新 free 和 len 属性的。
     * @param incr
     */
    // todo 这里是不是s应该去掉 java is s.method(incr) 对的 java中为对象.方法 所以不用将原来的对象放到那里面去。
    public void pStringIncLen(int incr) {
        if(this.getFree() < incr) {
            this.pStringResize(this.getLen(), incr);
        }
        this.setLen(this.getLen() + incr);
        this.setFree(this.getFree() - incr);
    }

    /**
     * 将 sds 扩充至指定长度，未使用的空间以 0 字节填充。
     * @param len
     */
    public PString pStringGrowZero(int len) {
        int curLen = this.getLen();
        byte[] curBuf = this.getBuf();
        int curFree = this.getFree();
        if(len <= curLen) return this;
        this.pStringMakeRoomFor(len - curLen);
        if(this == null) return null;

        byte[] initBuf = new byte[len];
        int totalLen = curLen + curFree;
        int initLen = len;
        int initFree = totalLen - len > 0 ? totalLen - len : 0;
        for(int i = 0; i < curLen; i++) {
            initBuf[i] = curBuf[i];
        }
        for(int i = curLen; i < len; i++) {
            initBuf[i] = 0;
        }
        PString resPString = PStringFactory.createPString(initLen, initFree, initBuf);
        this.setFree(resPString.getFree());
        this.setBuf(resPString.getBuf());
        this.setLen(len);
        return resPString;
    }

    /**
     * 将长度为 len 的字符串 t 追加到 sds 的字符串末尾
     * 返回值
     * sds ：追加成功返回新 sds ，失败返回 NULL
     * @return
     */
    public PString pStringCatLen(PString t, int len) {
        int curLen = this.getLen();
        this.pStringMakeRoomFor(len + curLen);
        if(this == null) return null;
        byte[] buf = this.getBuf();
        byte[] tBuf = t.getBuf();
        for(int i = curLen; i < len + curLen; i++) {
            buf[i] = tBuf[i - curLen];
        }
        this.setBuf(buf);
        this.setFree(0);
        this.setLen(len + curLen);
        return this;
    }

    public PString pStringCatPString(PString t) {
        return this.pStringCatLen(t, t.getLen());
    }

    public PString pStringCat(char[] c) {
        PString t = new PString();
        t.pStringNew(c);
        return this.pStringCatLen(t, t.getLen());
    }

    /**
     * 将字符串 t 的前 len 个字符复制到 sds s 当中，
     * 并在字符串的最后添加终结符。
     * 如果 sds 的长度少于 len 个字符，那么扩展 sds
     * @param t
     * @param len
     * @return
     */
    public PString pStringCpyLen(PString t, int len) {
        int totalLen = this.getFree() + this.getLen();
        if(totalLen < len) {
            this.pStringMakeRoomFor(len);
            if(this == null) return null;
            totalLen = this.getLen() + this.getFree();
        }
        byte[] curBuf = this.getBuf();
        byte[] tBuf = t.getBuf();
        System.arraycopy(tBuf, 0, curBuf, 0, len);
        this.setBuf(curBuf);
        this.setLen(len);
        this.setFree(totalLen - len);
        return this;
    }

    /**
     * sdstrim(sds s, const char *cset)
     * sdsrange(sds s, int start, int end)
     */

    public void pStringToLower() {
        int len = this.getLen();
        byte[] buf = this.getBuf();
        String str = new String(buf);
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < len; i++) {
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

    public void pStringToUpper() {
        int len = this.getLen();
        byte[] buf = this.getBuf();
        String str = new String(buf);
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < len; i++) {
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
     *  对比两个 sds ， strcmp 的 sds 版本
     *  返回值  int ：相等返回 0 ，s1 较大返回正数， s2 较大返回负数
     */
    public int pStringCmp(PString p2) {
        PString p1 = this;
        int len1 = p1.getLen(), len2 = p2.getLen();
        int cmp = 0;
        int minlen = len1 < len2 ? len1 : len2;
        byte[] buf1 = p1.getBuf();
        byte[] buf2 = p2.getBuf();
        for(int i = 0; i < minlen; i++) {
            if(buf1[i] > buf2[i]) {
                cmp = 1;
                break;
            } else if(buf1[i] < buf2[i]) {
                cmp = -1;
                break;
            }
        }
        return cmp;
    }

    public void pStringPrintBuf() {
        byte[] buf = this.getBuf();
        int len = this.getLen();
        for(int i = 0; i < len; i++) {
            System.out.print(buf[i]);
        }
        System.out.println();
    }

    public void pStringPrintLen() {
        int len = this.getLen();
        System.out.println("len:" + len);
    }

    public void pStringPrintFree() {
        int free = this.getFree();
        System.out.println("free:" +free);
    }

    public void pStringPrintAll() {
        this.pStringPrintBuf();
        this.pStringPrintLen();
        this.pStringPrintFree();
    }

}
