package protocol;

/**
 * @description: Pandis消息回复的5种类型
 * @author: huzihan
 * @create: 2021-07-22
 */
public enum ReplyType {

    ERROR {
        public String buildReplyByTemplate(String args) {
            StringBuilder message = new StringBuilder();
            message.append(ERROR_PREFIX);
            message.append(args);
            message.append(TERMINATOR);

            return message.toString();
        }
    },      // 错误信息
    STATUS {
        public String buildReplyByTemplate(String args) {
            StringBuilder message = new StringBuilder();
            message.append(STATUS_PREFIX);
            message.append(args);
            message.append(TERMINATOR);

            return message.toString();
        }

    },      // 状态回复
    INTEGER {
        public String buildReplyByTemplate(String args) {
            StringBuilder message = new StringBuilder();
            message.append(INTEGER_PREFIX);
            message.append(args);
            message.append(TERMINATOR);

            return message.toString();
        }
    },      // 整数回复
    BULK {
        public String buildReplyByTemplate(String args) {
            StringBuilder message = new StringBuilder();
            message.append(BULK_PREFIX);
            message.append(args.length());
            message.append(TERMINATOR);
            message.append(args);
            message.append(TERMINATOR);
            return message.toString();
        }
    },      // 字符串回复
    MULTI_BULK {
        // WARNING: 这里的实现有问题，由于多行回复可能需要传入很多数据，这里可能暂时难以构建
        public String buildReplyByTemplate(String args) {
            StringBuilder message = new StringBuilder();
            message.append(MULTI_BULK_PREFIX);
            message.append(args);
            message.append(TERMINATOR);

            return message.toString();
        }
    },
    NIL {
        public String buildReplyByTemplate(String args) {
            return null;
        }
    };      // 多行字符串回复

    public static final String ERROR_PREFIX = "-ERR ";
    public static final String STATUS_PREFIX = "+";
    public static final String INTEGER_PREFIX = ":";
    public static final String BULK_PREFIX = "$";
    public static final String MULTI_BULK_PREFIX = "*";
    public static final String TERMINATOR = "\r\n";

    /**
     * 基于模板，填入传入的参数生成回复消息
     * 不同的回复消息类型有不同的模板
     * @param args 参数
     * @return 构建的消息
     */
    public abstract String buildReplyByTemplate(String args);
}
