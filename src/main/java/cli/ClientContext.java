package cli;

import common.ErrorType;
import common.store.Sds;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import protocol.Protocol;
import protocol.Reply;
import protocol.ReplyParser;
import utils.SafeEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/7/25
 **/
public class ClientContext {
    private static final Log logger = LogFactory.getLog(ClientContext.class);

    private ErrorType err;
    private String errInfo;
    private Socket socket;
    private int flags;
    private StringBuilder outBuffer;    // 要发送的命令都存储在这里
    private Sds replyBuffer;            // 回复缓冲区
    private int replyBufferParsePos;    // 表示回复缓冲区中当前解析到的位置

    public ClientContext() {
        this.err = null;
        this.errInfo = null;
        this.socket = null;
        this.flags = 0;
        this.outBuffer = new StringBuilder();
        this.replyBuffer = Sds.newEmptySds();
        this.replyBufferParsePos = 0;
    }

    public void connectTcp(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
            // 设置socket的keepalive
            this.socket.setKeepAlive(true);

            logger.info("Connect to server " + ip + ":" + port);
        } catch (IOException e) {
            logger.fatal("Could not connect to Redis at " + ip + ":" + port + " Connection refused", e);
            System.exit(1);
        }
    }

    /**
     * 解析输入的命令参数，按照协议将其生成命令
     * 将命令缓存到客户端context的输出缓冲区outBuffer
     * @param startIndex 开始解析的位置
     * @param argv 参数数组
     * @return 操作是否成功
     */
    public boolean appendCommandArgv(int startIndex, final String [] argv) {
        String cmd = Protocol.formatCommand(startIndex, argv);
        if (cmd == null || "".equals(cmd)) {
            setErr(ErrorType.OOM_ERR, "Out of memory");
            return false;
        }

        if (!appendCommand(cmd)) {
            return false;
        }

        return true;
    }

    public String getErrInfo() {
        return this.errInfo;
    }

    public void setErr(ErrorType type, String str) {
        this.err = type;
        this.errInfo = str;
    }

    /**
     * 向缓冲区写入要发送的命令
     * @param cmd
     * @return
     */
    private boolean appendCommand(String cmd) {
        this.outBuffer.append(cmd);
        return true;
    }

    /**
     * 将outputBuffer中缓存的数据写入socket的输出流
     * 如果缓冲区为空或者成功发送了缓冲区的数据就返回true，如果发生异常则返回false
     * @return
     */
    public boolean write() {
        if (this.err != null) {
            return false;
        }

        if (this.outBuffer.length() > 0) {
            try {
                OutputStream outputStream = this.socket.getOutputStream();
                outputStream.write(SafeEncoder.encode(this.outBuffer.toString()));
                outputStream.flush();
                this.outBuffer.delete(0, this.outBuffer.length());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public boolean read() {
        if (this.err != null) {
            return false;
        }

        byte [] buf = new byte[1024 * 16];
        int readNum = 0;

        try {
            InputStream inputStream = this.socket.getInputStream();
            readNum = inputStream.read(buf);
            while (readNum != -1) {
                this.replyBuffer.cat(buf, 0, readNum);
                if (readNum < buf.length) {
                    break;
                }
                readNum = inputStream.read(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Reply parseItem() {
        char prefix = this.replyBuffer.charAt(0);
        byte [] buf = this.replyBuffer.getBuf();
        Reply reply = null;
        switch (prefix) {
            case '-':
                reply = ReplyParser.parseErrorReply(buf, this.replyBufferParsePos);
                break;
            case '+':
                reply = ReplyParser.parseStatusReply(buf, this.replyBufferParsePos);
                break;
            case ':':
                reply = ReplyParser.parseIntegerReply(buf, this.replyBufferParsePos);
                break;
            case '$':
                reply = ReplyParser.parseBulkReply(buf, this.replyBufferParsePos);
                break;
            case '*':
                reply = ReplyParser.parseMultiBulkReply(buf, this.replyBufferParsePos);
                break;
            default:
                // 异常情况
        }

        // 调整缓冲区大小
        this.replyBufferParsePos += reply.getParseByteLength();

        if (this.replyBufferParsePos == this.replyBuffer.getLen()) {
            this.replyBufferParsePos = 0;
            this.replyBuffer.resize(0,1024 * 16);
        }

        return reply;
    }


    public Reply getReplyFromBuffer() {
        if (this.replyBuffer.getLen() > 0 && this.replyBufferParsePos != this.replyBuffer.getLen()) {
            return parseItem();
        } else {
            return null;
        }
    }

    public Reply getReply() {
        Reply reply = getReplyFromBuffer();
        if (reply != null) {
            return reply;
        } else {
            if (write()) {
                if (read()) {
                    reply = getReplyFromBuffer();
                }
            }

            return reply;
        }
    }
}
