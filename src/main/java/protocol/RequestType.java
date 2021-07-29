package protocol;

/**
 * @description: 请求类型
 * @author: huzihan
 * @create: 2021-07-19
 */
public enum RequestType {
    NONE,
    MULTI_BULK, // 统一请求协议类型
    INLINE;     // 简单协议类型

    // 统一亲求协议类型前缀
    public static final char MULTI_BULK_PREFIX = '*';
}
