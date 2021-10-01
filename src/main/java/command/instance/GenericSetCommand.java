package command.instance;

import command.AbstractCommand;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-09-27
 */
public abstract class GenericSetCommand extends AbstractCommand {

    //  表示set参数的flags
    public static final int SET_NO_FLAGS = 0;   // 没有标记
    public static final int SET_NX = 1 << 0;    // 当key不存在时可以set
    public static final int SET_XX = 1 << 1;    // 当key存在时可以set

    public GenericSetCommand(String name, int arity, boolean isGreaterThanArity, String stringFlags) {
        super(name, arity, isGreaterThanArity, stringFlags);
    }
//
//    protected void genericSet(PandisClient client, int flags, PandisObject key, PandisObject value, PandisObject expire, TimeUnit unit) {
//        long milliseconds = 0;
//        // 如果设置了过期时间，则先解析过期时间
//        if (expire != null) {
//            // 取出expire参数的值
//            try {
//                milliseconds = expire.getLong();
//            } catch (OperationNotSupportedException e) {
//                return;
//            }
//
//            // 验证expire参数的值
//            if (milliseconds <= 0) {
//                client.addReply(ReplyType.ERROR, "invalid expire time in SETEX");
//                return;
//            }
//
//            // 不论输入的过期时间是秒还是毫秒
//            // Redis 实际都以毫秒的形式保存过期时间
//            // 如果输入的过期时间为秒，那么将它转换为毫秒
//            if (unit == TimeUnit.SECONDS) {
//                milliseconds *= 1000;
//            }
//        }
//
//        // 如果设置了 NX 或者 XX 参数，那么检查条件是否不符合这两个设置
//        // 在条件不符合时报错，报错的内容由 abort_reply 参数决定
//        if ((flags & SET_NX && ) || (flags & SET_XX && )) {
//            return;
//        }
//
//        Database database = client.getDatabase();
//        // 将键值关联到数据库
//        database.setKey(key, value);
//
//        // 将数据库设为脏
//
//        // 为键设置过期时间
//        if (expire != null) {
//            database.setExpire(key, System.currentTimeMillis() + milliseconds);
//        }
//
//        // 发送事件通知
//
//        // 发送事件通知
//
//        // 设置成功，向客户端发送回复
//        // 回复的内容由 ok_reply 决定
//    }
}
