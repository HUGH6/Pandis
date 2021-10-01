package remote;

import common.struct.PandisObject;
import common.struct.impl.Sds;

/**
 * 表示服务器向客户端进行消息回复的接口
 * @Author huzihan
 * @Date 2021/9/28
 **/
public interface Replyer {
    void reply(Reply reply, Object obj);
}
