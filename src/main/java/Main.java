import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-17
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("127.0.0.1", 6379));
        sc.configureBlocking(false);
        sc.socket().setKeepAlive(true);

        ByteBuffer buf = ByteBuffer.allocate(1024*16);

        int n = sc.read(buf);

        while (n != -1) {
            System.out.println("youshuru");
            buf.flip();
            buf.clear();

            n = sc.read(buf);
        }

        System.out.println(n);

        while(true) {
            Thread.sleep(1000);
        }
    }
}
