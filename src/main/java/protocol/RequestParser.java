package protocol;

import client.PandisClient;
import common.store.ObjectType;
import common.store.Sds;
import common.store.StoreObject;
import utils.SdsUtil;

import javax.xml.bind.SchemaOutputResolver;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * @description: 该类负责解析客户端发送来的查询请求
 * @author: huzihan
 * @create: 2021-07-19
 */
public class RequestParser {

    public static final int  INLINE_MAX_SIZE  = 1024 * 64;  /* Max size of inline reads */
    public static final int  MBULK_BIG_ARG = 1024 * 32;

    /**
     * 处理内联查询格式
     * 内联命令的各个参数以空格分开，并以 \r\n 结尾
     * @param client 缓存请求数据的客户端
     * @return 解析是否成功
     */
    public static boolean processInlineRequest(PandisClient client) {
        // Search for end of line
        Sds queryBuffer = client.getQueryBuffer();
        int indexLast = queryBuffer.indexOf('\n');

        // 收到的查询内容不符合协议内容，出错
        if(indexLast == -1) {
            if(queryBuffer.getLen() > INLINE_MAX_SIZE) {
                // todo 错误处理 ，
                client.addReply(ReplyType.ERROR, "Protocol error: too big inline request");
            }
            return false;
        }

        // handle the \r\n case
        if (indexLast != 0 || queryBuffer.charAt(indexLast - 1) == '\r'){
            indexLast--;
        }

        // 根据空格，分割命令的参数
        int queryLen = indexLast;
        Sds request = Sds.createSds(queryLen, queryBuffer.getBufNoCopy());
        Sds[] argv = SdsUtil.splitArgs(request);

        if(argv == null){
            //todo 错误处理， Protocol error: unbalanced quotes in request
            client.addReply(ReplyType.ERROR, "Protocol error: unbalanced quotes in request");
            return false;
        }

        /** todo
        * Newline from slaves can be used to refresh the last ACK time.
        * This is useful for a slave to ping back while loading a big
        * RDB file. */

        // 从缓冲区中删除已从 argv 已读取的内容，剩余内容时未读取的
        client.getQueryBuffer().cut(queryLen + 2, queryBuffer.getLen());

        StoreObject[] so = new StoreObject[argv.length];
        // 为每个参数创建一个字符串对象
        for(int i = 0; i < argv.length; i++){
            if(argv[i].getLen() > 0){
                so[i] = new StoreObject(ObjectType.STRING, argv[i]);
            }
        }

        client.setArgv(so);

        System.out.println("测试：");
        System.out.println(Arrays.toString(argv));

        return true;
    }

    /**
     * 处理多条查询格式
     * 比如 *3\r\n$3\r\nSET\r\n$3\r\nMSG\r\n$5\r\nHELLO\r\n
     * 将被转换为：
     * argv[0] = SET
     * argv[1] = MSG
     * argv[2] = HELLO
     * @param client 缓存请求数据的客户端
     * @return 解析是否成功
     */
    public static boolean processMultiBulkRequest(PandisClient client) {
        int index;
        int pos = 0, ok;
        int requestItemNum = 0;
        // 解析读入命令的参数个数
        // 比如 *3\r\n$3\r\nSET\r\n... 将令 c->multibulklen = 3
        if (client.getMultiBulkLen() == 0) {

            // todo The client should have been reset

            // 检查缓冲区的内容第一个 \r\n eg:*3\r\n
            Sds queryBuffer = client.getQueryBuffer();
            index = queryBuffer.indexOf('\r');
            if (index == -1) {
                if (queryBuffer.getLen() > INLINE_MAX_SIZE) {
                    client.addReply(ReplyType.ERROR, "Protocol error: too big mbulk count string");
                    return false;
                }
            }

            // Buffer should also contain \n
            if (index > queryBuffer.getLen() - 2) {
                return false;
            }

            // todo 协议的第一个字符必须是 '*'
            if (queryBuffer.charAt(0) != '*') {
                return false;
            }

            requestItemNum = Integer.parseInt(new String(queryBuffer.getBufNoCopy(), 1, index - 1));
            if(requestItemNum > 1024 * 1024){
                client.addReply(ReplyType.ERROR, "Protocol error: invalid multibulk length");
                return false;
            }

            // 参数数量之后的位置
            // 比如对于 *3\r\n$3\r\n$SET\r\n... 来说，
            // pos 指向 *3\r\n$3\r\n$SET\r\n...
            //               ^
            //               |
            //              pos
            // 参数数量之后的位置
            pos = index + 2;

            client.setMultiBulkLen(requestItemNum);
        }

        // 从 c->querybuf 中读入参数，并创建各个参数对象到 c->argv
        while (client.getMultiBulkLen() > 0){

            Sds queryBuffer = client.getQueryBuffer();

            // 读入参数长度
            if(client.getBulkLen() == -1) {
                // 确保 "\r\n" 存在
                index = queryBuffer.indexOf(pos, '\r');
                if (index == -1) {
                    if (queryBuffer.getLen() > INLINE_MAX_SIZE) {
                        // todo Protocol error: too big bulk count string
                        client.addReply(ReplyType.ERROR, "Protocol error: too big bulk count string");
                        return false;
                    }
                    break;
                }
                // Buffer should also contain \n
                if (index > queryBuffer.getLen() - 2) {
                    break;
                }
                // 确保协议符合参数格式，检查其中的 $...
                // 比如 $3\r\nSET\r\n
                if ((char) queryBuffer.charAt(pos) != '$') {
                    // todo "Protocol error: expected '$', got '%c'"
                    client.addReply(ReplyType.ERROR, "Protocol error: expected '$'");
                    return false;
                }
                // 读取长度
                requestItemNum = Integer.parseInt(new String(queryBuffer.getBufNoCopy(), pos + 1, index - pos - 1));
                if (requestItemNum < 0 || requestItemNum > 512 * 1024 * 1024) {
                    // todo Protocol error: invalid bulk length
                    client.addReply(ReplyType.ERROR, "Protocol error: invalid bulk length");
                    return false;
                }
                // 定位到参数的开头
                pos = index + 2;

                // 如果参数非常长，那么做一些预备措施来优化接下来的参数复制操作
                if (requestItemNum >= MBULK_BIG_ARG) {
                    // todo
                }
                // 参数的长度
                client.setBulkLen(requestItemNum);
            }
            // 读入参数
            if (queryBuffer.getLen() - pos < client.getBulkLen() + 2) {
                // 确保内容符合协议格式
                // 比如 $3\r\nSET\r\n 就检查 SET 之后的 \r\n
                break;
            } else {
                // 为参数创建字符串对象
                if (pos == 0 && client.getBulkLen() >= MBULK_BIG_ARG && queryBuffer.getLen() == client.getBulkLen() + 2){
                    // todo
                } else {
                    StoreObject[] storeObject = new StoreObject[client.getArgv().length+1];
                    System.arraycopy(client.getArgv(), 0, storeObject, 0, client.getArgv().length);
                    storeObject[storeObject.length-1] = new StoreObject(ObjectType.STRING, Sds.createSds(new String(queryBuffer.getBuf(), pos, client.getBulkLen()).getBytes(StandardCharsets.UTF_8)));
                    client.setArgv(storeObject);
                    pos += client.getBulkLen() + 2;
                }
                client.setBulkLen(-1);
                client.setMultiBulkLen(client.getMultiBulkLen()-1);
            }
        }
        // 从 querybuf 中删除已被读取的内容
        if (pos > 0) {
            Sds queryBuffer = client.getQueryBuffer();
            queryBuffer.cut(pos, queryBuffer.getLen());
        }

        // 如果本条命令的所有参数都已读取完，那么返回
        if (client.getMultiBulkLen() == 0) {
            System.out.println("测试");
            System.out.println(Arrays.toString(client.getArgv()));
            return true;
        }

        // 如果还有参数未读取完，那么就协议内容有错
        return false;
    }

}
