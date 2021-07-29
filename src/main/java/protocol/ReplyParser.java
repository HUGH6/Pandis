package protocol;

import utils.SafeEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description 回复解析器
 * @Author huzihan
 * @Date 2021/7/27
 **/
public class ReplyParser {
    /**
     * 解析整数类型回复
     * @param bytes
     * @param start
     * @return
     */
    public static Reply parseIntegerReply(byte [] bytes, int start) {
        if (start < 0 || start >= bytes.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 检测传入的数据是否是整数回复格式
        if ((char)bytes[start] != ':') {
            throw new IllegalStateException("Parse bytes as integer error");
        }

        int end = seekNewLine(bytes, start);
        // 跳过'$'
        int index = start + 1;
        if (end != -1 && index < end) {
            long content = parseInteger(bytes, index, end);
            int parsedByteLength = end - start + 2;
            Reply reply = new Reply(ReplyType.INTEGER, content, parsedByteLength);
            return reply;
        } else {
            throw new IllegalStateException("Parse bytes as integer error");
        }
    }

    public static Reply parseErrorReply(byte [] bytes, int start) {
        if (start < 0 || start >= bytes.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 检测传入的数据是否是错误回复格式
        if ((char)bytes[start] != '-') {
            throw new IllegalStateException("Parse bytes as err error");
        }

        // 跳过'-'
        int index = start + 1;
        int end = seekNewLine(bytes, start);
        if (end != -1 && index < end) {
            byte [] replay = Arrays.copyOfRange(bytes, index, end);
            String content = SafeEncoder.encode(replay);
            int parsedByteLength = end - start + 2;
            Reply reply = new Reply(ReplyType.ERROR, content, parsedByteLength);
            return reply;
        } else {
            throw new IllegalStateException("Parse bytes as err error");
        }
    }

    public static Reply parseStatusReply(byte [] bytes, int start) {
        if (start < 0 || start >= bytes.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 检测传入的数据是否是状态回复格式
        if ((char)bytes[start] != '+') {
            throw new IllegalStateException("Parse bytes as status error");
        }

        // 跳过'+'
        int index = start + 1;
        int end = seekNewLine(bytes, start);
        if (end != -1 && index < end) {
            byte [] replay = Arrays.copyOfRange(bytes, index, end);
            String content = SafeEncoder.encode(replay);
            int parsedByteLength = end - start + 2;
            Reply reply = new Reply(ReplyType.STATUS, content, parsedByteLength);
            return reply;
        } else {
            throw new IllegalStateException("Parse bytes as status error");
        }
    }

    public static Reply parseBulkReply(byte [] bytes, int start) {
        if (start < 0 || start >= bytes.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 检测传入的数据是否是BULK回复格式
        if ((char)bytes[start] != '$') {
            throw new IllegalStateException("Parse bytes as status error");
        }

        // 跳过'+'
        int index = start + 1;
        int end = seekNewLine(bytes, start);

        if (end != -1 && index < end) {
            // 先解析bulk后面的实际内容字节长度
            int len = (int)parseInteger(bytes, index, end);

            if (len < 0) {
                Reply reply = new Reply(ReplyType.NIL, "nil", 5);
                return reply;
            }

            // 跳过$n这里的\r\n
            int nextStart = end + 2;
            int nextEnd = nextStart + len;

            if (nextEnd > bytes.length) {
                return null;
            }

            byte [] replay = Arrays.copyOfRange(bytes, nextStart, nextEnd);
            String content = SafeEncoder.encode(replay);
            int parsedByteLength = nextEnd - start + 2;
            Reply reply = new Reply(ReplyType.BULK, content, parsedByteLength);
            return reply;
        }

        return null;
    }

    public static Reply parseMultiBulkReply(byte [] bytes, int start) {
        if (start < 0 || start >= bytes.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 检测传入的数据是否是状态回复格式
        if ((char)bytes[start] != '*') {
            throw new IllegalStateException("Parse bytes as multi bulk error");
        }

        // 跳过'*'
        int index = start + 1;
        int end = seekNewLine(bytes, start);
        if (end != -1 && index < end) {
            int num = (int) parseInteger(bytes, index, end);
            if (num < 0) {
                return null;
            }
            index = index + 3;
            List<String> ret = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                if (index < bytes.length) {
                    Reply reply = parseBulkReply(bytes, index);
                    ret.add(reply.getStringReplyContent());
                    index = index + reply.getParseByteLength();
                }
            }

            int parsedByteLength = index - start;
            Reply reply = new Reply(ReplyType.MULTI_BULK, ret, parsedByteLength);
            return reply;
        }
        return null;
    }

    /**
     * 找到从start位置开始往后的第一个分割符\r\n
     * @param bytes 要查找的字节数组
     * @param start 查找开始的位置
     * @return start开始第一个\r\n的位置，即\r所在的位置
     */
    public static int seekNewLine(byte [] bytes, int start) {
        if (start < 0 || start >= bytes.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        int index = start;
        while (index < bytes.length) {
            while (index < bytes.length && (char)bytes[index] != '\r') {
                index++;
            }

            if (index >= bytes.length || (char)bytes[index] != '\r') {
                return -1;
            } else {
                if (index + 1 < bytes.length && (char)bytes[index + 1] == '\n') {
                    return index;
                } else {
                    index++;
                }
            }
        }

        return -1;
    }

    /**
     * 将字节数组从[start,end)范围内的字符转换为整数
     * 如果在整个数组中有不是数字的字符，则解析过程会截止，直接返回已经解析到的数值
     * @param bytes
     * @param start
     * @param end
     * @return
     */
    public static long parseInteger(byte [] bytes, int start, int end) {
        if (start < 0 || end > bytes.length || start > end) {
            throw new IllegalArgumentException();
        }

        int index = start;
        boolean negative = false;
        if ((char)bytes[index] == '-') {
            negative = true;
            index++;
        }

        long value = 0;
        while (index < end) {
            char ch = (char)bytes[index];
            if (Character.isDigit(ch)) {
                value = value * 10 + Character.digit(ch, 10);
                index++;
            } else {
                break;
            }
        }
        return negative ? -1 * value : value;
    }
}
