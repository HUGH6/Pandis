import common.store.ZipList;
import lombok.SneakyThrows;
import utils.ByteUtil;
import utils.SafeEncoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-17
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
//        print(ZipList.STR_6BIT);
//        print(ZipList.STR_14BIT);
//        print(ZipList.STR_32BIT);
//
//        byte a = 0b0101_0110;
//        byte b = (byte)0b1001_0110;
//
//        print((byte)(ZipList.STR_6BIT & ZipList.INT_MASK));
//        print((byte)(ZipList.STR_14BIT & ZipList.INT_MASK));
//        print((byte)(ZipList.STR_32BIT & ZipList.INT_MASK));
//
//        print((byte)(a & ZipList.INT_MASK));
//        print((byte)(b & ZipList.INT_MASK));

//        print(ZipList.INT_8BIT);
//        print(ZipList.INT_16BIT);
//        print(ZipList.INT_24BIT);
//        print(ZipList.INT_32BIT);
//        print(ZipList.INT_64BIT);

//        byte a = 0b0100_0010;
//        byte b = (byte)0b0000_1111;
//
//        byte [] bytes = new byte[]{(byte)(a & 0x3f), b};
//        short r = ByteUtil.bytesToShort(bytes);
//
//        System.out.println(r);
//
//        byte c = 0b0000_0010;
//        byte d = 0b0000_0001;
//        r = ByteUtil.bytesToShort(new byte[]{c, d});
//        System.out.println(r);

        int [] a = new int [] {1, 2, 3 , 4, 5, 0, 0};
        System.arraycopy(a, 1, a, 1 + 2, 5 - 1);
        System.out.println(Arrays.toString(a));


    }

    public static void print(byte b) {
        System.out.println(Arrays.toString(getBooleanArray(b)));
    }

    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }
    /**
     * 把byte转为字符串的bit
     */
    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }
}
