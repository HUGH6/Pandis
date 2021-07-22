package protocol;

import client.PandisClient;
import common.store.ObjectType;
import common.store.Sds;
import common.store.StoreObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-19
 */
public class RequestProcessor {

    public static final int  PANDIS_INLINE_MAX_SIZE  = 1024*64;  /* Max size of inline reads */
    public static final int  PANDIS_MBULK_BIG_ARG = 1024*32;

    /**
     * 处理内联查询格式
     * @param client
     * @return
     */
    public static boolean processInlineRequest(PandisClient client) {

        int argc = 0, queryLen, indexLast;
        Sds aux;
        Sds[] argv;

        // Search for end of line
        Sds queryBuffer = client.getQueryBuffer();
        indexLast = queryBuffer.indexOf('\n');

        // 收到的查询内容不符合协议内容，出错
        if(indexLast == -1){
            if(queryBuffer.getLen() > PANDIS_INLINE_MAX_SIZE){
                // todo 错误处理 ， Protocol error: too big inline request
            }
            return false;
        }

        // handle the \r\n case
        if (indexLast != 0 || queryBuffer.charAt(indexLast - 1) == '\r'){
            indexLast--;
        }

        // 根据空格，分割命令的参数
        queryLen = indexLast;
        aux = Sds.createSds(queryLen, queryBuffer.getBuf());
        argv = Sds.splitSds(aux);

        if(argv == null){
            //todo 错误处理， Protocol error: unbalanced quotes in request
            return false;
        }

        // todo 从设备的换行符可用于刷新最后的 ACK 时间。 这对于从站在加载大 RDB 文件时 ping 回很有用。

        // 从缓冲区中删除已从 argv 已读取的内容
        // todo 优化
        client.setQueryBuffer(Sds.createSds(queryBuffer.getLen(), Arrays.copyOfRange(queryBuffer.getBuf(), queryLen + 2, queryBuffer.getLen())));

        StoreObject[] so = new StoreObject[argv.length];
        // 为每个参数创建一个字符串对象
        for(int i = 0; i < argv.length; i++){
            if(argv[i].getLen() > 0){
                so[argc++] = new StoreObject(ObjectType.STRING, argv[i]);
            }
        }
        client.setArgc(argc);
        client.setArgv(so);
        return true;
    }

    /**
     * 处理多条查询格式
     * @param client
     * @return
     */
    public static boolean processMultiBulkRequest(PandisClient client) {
        // Todo
        int index;
        int pos = 0, ok;
        int l;

        // 解析读入命令的参数个数
        if(client.getMultiBulkLen() == 0){

            // todo The client should have been reset

            // 检查缓冲区的内容第一个 \r\n eg:*3\r\n
            Sds queryBuffer = client.getQueryBuffer();
            index = queryBuffer.indexOf('\r');
            if(index == -1){
                // todo 错误处理， Protocol error: too big mbulk count string
                return false;
            }
            // Buffer should also contain \n
            if(index > queryBuffer.getLen()-2){
                return false;
            }
            // todo 协议的第一个字符必须是 '*'

            l = Integer.parseInt(new String(queryBuffer.getBuf(), 1, index - 1));
            if(l > 1024 * 1024){
                // todo Protocol error: invalid multibulk length
                return false;
            }

            // 参数数量之后的位置
            pos = index + 2;

            if(l <= 0){
                client.setQueryBuffer(Sds.createSds(queryBuffer.getLen(), Arrays.copyOfRange(queryBuffer.getBuf(), pos, queryBuffer.getLen())));
            }
            client.setMultiBulkLen(l);
        }

        // todo assert multibulklen > 0

        // 从 c->querybuf 中读入参数，并创建各个参数对象到 c->argv
        while (client.getMultiBulkLen() > 0){

            Sds queryBuffer = client.getQueryBuffer();

            // 读入参数长度
            if(client.getBulkLen() == -1) {
                // 确保 "\r\n" 存在
                index = queryBuffer.indexOf(pos, '\r');
                if (index == -1) {
                    if (queryBuffer.getLen() > PANDIS_INLINE_MAX_SIZE) {
                        // todo Protocol error: too big bulk count string
                        return false;
                    }
                    break;
                }
                // Buffer should also contain \n
                if (index > queryBuffer.getLen() - 2) {
                    break;
                }
                // 确保协议符合参数格式，检查其中的 $
                if ((char) queryBuffer.getBuf()[pos] != '$') {
                    // todo "Protocol error: expected '$', got '%c'"
                    return false;
                }
                // 读取长度
                l = Integer.parseInt(new String(queryBuffer.getBuf(), pos + 1, index - pos - 1));
                if (l < 0 || l > 512 * 1024 * 1024) {
                    // todo Protocol error: invalid bulk length
                    return false;
                }
                // 定位到参数的开头
                pos = index + 2;

                // 如果参数非常长，那么做一些预备措施来优化接下来的参数复制操作
                if (l >= PANDIS_MBULK_BIG_ARG) {
                    // todo
                }
                // 参数的长度
                client.setBulkLen(l);
            }
            // 读入参数
            if(queryBuffer.getLen() - pos < client.getBulkLen() + 2){
                // 确保内容符合协议格式
                break;
            }else{
                if(pos == 0 && client.getBulkLen() >= PANDIS_MBULK_BIG_ARG && queryBuffer.getLen() == client.getBulkLen() + 2){
                    // todo
                }else{
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
        if(pos > 0){
            Sds queryBuffer = client.getQueryBuffer();
            client.setQueryBuffer(Sds.createSds(queryBuffer.getLen(), Arrays.copyOfRange(queryBuffer.getBuf(), pos, queryBuffer.getLen())));
        }

        // 如果本条命令的所有参数都已读取完，那么返回
        if(client.getMultiBulkLen() == 0){
            return true;
        }

        // 如果还有参数未读取完，那么就协议内容有错
        return false;
    }

}
