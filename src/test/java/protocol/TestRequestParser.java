package protocol;

import client.PandisClient;
import common.store.Sds;
import common.store.PandisObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName TestRequestProcessor
 * @Description
 * @Author huangyaohua
 * @Date 2021-07-22 17:16
 * @Version
 */
public class TestRequestParser {
    @Test
    public void testProcessInlineRequest() {
        Sds s = Sds.createSds("SET msg hello \r\n ".getBytes(StandardCharsets.UTF_8));
        System.out.println(s.toString());

//        RequestProcessor.processInlineRequestTest(s);
    }

    @Test
    public void testProcessMultiBulkRequest() throws IOException {
        final SocketChannel socketChannel = SocketChannel.open();
        final PandisClient client = PandisClient.createClient(null);
        final Sds sds = Sds.createSds("*3\r\n$3\r\nSET\r\n$3\r\nMSG\r\n$5\r\nHELLO\r\n".getBytes(StandardCharsets.UTF_8));
        client.setQueryBuffer(sds);

//        RequestProcessor.processMultiBulkRequestTest(client);
        final PandisObject[] argv = client.getArgv();
        for(int i = 0; i < argv.length; i++){
            System.out.println(argv[i].getObj().toString());
        }

    }
}
