package protocol;

import java.util.List;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/7/27
 **/
public class Reply {
    private ReplyType type;                         // 回复类型
    private int parseByteLength;                    // 解析的回复信息总共的字节长度
    private String stringReplyContent;              // 存放error、status、bulk类型的回复的实际内容
    private long integerReplyContent;               // 存放integer类型回复的实际内容
    private List<String> multiStringReplyContent;   // 存放multi-bulk类型回复的实际内容

    public Reply(ReplyType type, String str, int len) {
        this.type = type;
        this.stringReplyContent = str;
        this.parseByteLength = len;
        this.integerReplyContent = 0;
        this.multiStringReplyContent = null;
    }

    public Reply(ReplyType type, long num, int len) {
        this.type = type;
        this.stringReplyContent = null;
        this.parseByteLength = len;
        this.integerReplyContent = num;
        this.multiStringReplyContent = null;
    }

    public Reply(ReplyType type, List<String> multi, int len) {
        this.type = type;
        this.stringReplyContent = null;
        this.parseByteLength = len;
        this.integerReplyContent = 0;
        this.multiStringReplyContent = multi;
    }

    public ReplyType getType() {
        return this.type;
    }

    public int getParseByteLength() {
        return this.parseByteLength;
    }

    public long getIntegerReplyContent() {
        if (this.type != ReplyType.INTEGER) {
            throw new IllegalStateException();
        }
        return this.integerReplyContent;
    }

    public String getStringReplyContent() {
        if (this.type != ReplyType.ERROR && this.type != ReplyType.STATUS && this.type != ReplyType.BULK) {
            throw new IllegalStateException();
        }
        return this.stringReplyContent;
    }

    public List<String> getMultiStringReplyContent() {
        if (this.type != ReplyType.MULTI_BULK) {
            throw new IllegalStateException();
        }
        return this.multiStringReplyContent;
    }
}
