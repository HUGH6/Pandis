package common.expire;

import common.struct.PandisString;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/4
 **/
public interface PeriodicExpiration {
    int FAST_MODE = 0;
    int SLOW_MODE = 1;
    int EXPIRE_CYCLE_FAST_DURATION = 1000;

    void delExpiredPeriodicaly(int mode);

}
