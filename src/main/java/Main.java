import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-17
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
//        Socket socket = new Socket();
//        socket.connect(new InetSocketAddress("127.0.0.1", 6379));
//
//        Thread.sleep(5000);
//
//        OutputStream out =  socket.getOutputStream();
//
//        OutputStreamWriter ow = new OutputStreamWriter(out);
//        ow.write("hello");
//        ow.flush();
//        Thread.sleep(500);
//        ow.write("world world world world");
//        ow.flush();
//        Thread.sleep(500);
//        socket.close();

//
//        Thread.sleep(5000);
//
//        Socket s = new Socket("127.0.0.1", 6379);
//        out =  s.getOutputStream();
//        ow = new OutputStreamWriter(out);
//
//        ow.write("hello");
//        ow.flush();
//
//        Thread.sleep(5000);
//
//        System.exit(3);
        char a = 'a';
        byte b = 1;

        String s1=Integer.toBinaryString(a);
        String s2=Integer.toBinaryString(b);
        System.out.println(s1);
        System.out.println(s2);

        byte c = 'c';
        char d = (char)c;
        System.out.println(Integer.toBinaryString(d & 0xff));
        System.out.println((char)(d & 0xff));


    }



}
