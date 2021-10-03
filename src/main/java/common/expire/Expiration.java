package common.expire;

import common.struct.PandisString;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/3
 **/
public interface Expiration {
    boolean isExpired(PandisString key);
}
