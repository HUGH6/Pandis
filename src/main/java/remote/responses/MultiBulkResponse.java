package remote.responses;

import remote.Reply;
import remote.Response;
import remote.protocol.Protocol;
import remote.protocol.ReplyType;
import utils.SafeEncoder;

import java.util.List;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/9/28
 **/
public class MultiBulkResponse implements Response {
    private final List<String> content;
    private final int byteSize;

    public MultiBulkResponse(List<String> multi, int byteSize) {
        this.content = multi;
        this.byteSize = byteSize;
    }

    @Override
    public ReplyType getType() {
        return ReplyType.MULTI_BULK;
    }

    @Override
    public int byteSize() {
        return this.byteSize;
    }

    @Override
    public Object getContent() {
        return content;
    }

}
