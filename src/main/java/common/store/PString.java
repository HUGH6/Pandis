package common.store;

import lombok.Data;

@Data
public class PString {
    private Sds sds;

    /**
     * 根据给定的初始化字符串 init 和字符串长度 initlen
     * @param init 初始化字符串指针
     * @param initlen 初始化字符串的长度
     * @return 创建成功返回 sdshdr 相对应的 sds 创建失败返回 NULL
     */
    public PString pStringNewLen(char[] init, int initlen) {
        // todo this 与 pString 区别??
        PString pString = new PString();

        // 根据是否有初始化内容，选择适当的内存分配方式
        if(init.length != 0) {
            this.getSds().setBuf(new char[this.sds.getLen() + initlen + 1]);
        } else {
            this.getSds().setBuf(new char[initlen + 1]);
        }
        // 内存分配失败，返回
        if(this.sds.isEmpty()) {
            return pString.sdsEmpty();
        }
        // 设置初始化长度
        this.getSds().setLen(initlen);
        // 新 sds 不预留任何空间
        this.getSds().setFree(0);
        // 如果有指定初始化内容，将它们复制到 sdshdr 的 buf 中
        if(initlen != 0 && init.length != 0) {
            memcpy(this.getSds().getBuf(), init, initlen);
        }
        char[] target = this.getSds().getBuf();
        target[initlen] = '\0';
        this.getSds().setBuf(target);
        return this;
    }

    /**
     * 复制init 中长initlen到buf中
     * @param buf 目标数组
     * @param init 数据来源数组
     * @param initlen 初始化长度
     */
    public void memcpy(char[] buf, char[] init, int initlen) {
        // todo 需要考虑覆盖吗 每次都是new 新的字符数组
        int buflen = buf.length;
        int fromlen = init.length;
        int len = Math.min(fromlen, initlen);
        if(len > buflen) {
            this.getSds().setBuf(init);
        } else {
            char[] target = this.getSds().getBuf();
            for(int i = 0; i < len; i++) {
                target[i] = init[i];
            }
            this.getSds().setBuf(target);
        }
    }

    /**
     * 创建并返回一个只保存了空字符串 "" 的 sds
     * @return  创建成功返回 sdshdr 相对应的 sds 创建失败返回 NULL
     */
    public PString sdsEmpty() {
        char[] init = new char[0];
        return pStringNewLen(init, 0);
    }

    /**
     * 根据给定字符串 init ，创建一个包含同样字符串的 sds
     * @param init 给定字符串 init
     * @return
     */
    public PString pStringNew(char[] init) {
        int initlen = init.length == 0 ? 0 : init.length;
        return pStringNewLen(init, initlen);
    }

    /**
     * 复制给定 pString 的副本
     * @param pString 给定 pString
     * @return 创建成功返回输入 pString 的副本 创建失败返回 NULL
     */
    public PString pStringUp(PString pString) {
        return pStringNewLen(pString.getSds().getBuf(), pString.getSds().getLen());
    }

//    abstract public void sdsfree(sds s);
//    abstract public sds sdsgrowzero(sds s, int len);
//
//    /**
//     * size_t sdslen(const sds s);
//     * size_t sdsavail(const sds s);
//     * sds sdscatvprintf(sds s, const char *fmt, va_list ap);
//     */
//
//    /**
//     * 将chararr数组追加到s后妈
//     * @param s
//     * @return
//     */
//    abstract public sds sdscatlen(sds s, char[] chararr);
//    abstract public sds sdscpy(sds s, char[] chararr);
//    abstract public sds sdscatvprintf(sds s);
//    abstract public sds sdscatprintf(sds[] s);
//    abstract public sds sdscatfmt(sds s, char[] chararr);
//    abstract public sds sdstrim(sds s, char[] trim);
//    abstract public sds sdsrange(sds s, int start, int end);
//    abstract public void sdsupdatelen(sds s);
//    abstract public void sdsclear(sds s);
//    abstract public int sdscmp(sds s1, sds s2);
//    abstract public sds sdssplitlen(char[] s, int len, char[] sep, int seplen, int count);
//    abstract public void sdsfreesplitres(sds tokens, int count);
//    abstract public void sdstolower(sds s);
//    abstract public void sdstoupper(sds s);
//    abstract public sds sdsfromlonglong(Long value);
//    abstract public sds sdscatrepr(sds s, char[] p, int len);
//    abstract public sds sdssplitargs(char[] line, int argc);
//    abstract public sds sdsmapchars(sds s, char[] from, char[] to, int setlen);
//    abstract public sds sdsjoin(char[][] argv, int argc, char[] sep);
//
//    /* Low level functions exposed to the user API */
//    abstract public sds sdsMakeRoomFor(sds s, int addlen);
//    abstract public void sdsIncrLen(sds s, int incr);
//    abstract public sds sdsRemoveFreeSpace(sds s);
//    abstract public int sdsAllocSize(sds s);


}
