package common.struct.impl;

import common.utils.ByteUtil;

import java.util.Arrays;

/**
 * 压缩列表，以内存经凑的方式顺序存储元素
 * ZipList其实就是一块连续的内存空间，列表的相关属性和元素都被以紧凑的方式编排在这块内存空间中
 *
 * 注意：由于redis中用无符号整数类型来记录各种长度信息，而java中没有无符号类型，因此能记录的大小比redis中少了一半（符号位占了1位）
 *
 * ZipList结构：
 * ------------------------------------------------------------------------
 * |   bytes   |    tail    | size | entry1 | entry2 | ... | entryN | NED |
 * ------------------------------------------------------------------------
 *      ^            ^        ^        ^                              ^
 *      |            |        |        |                              |
 * 列表占总字节数  尾节点位置   元素个数  列表元素                        列表末尾标记
 *  （4字节）    （4字节）   （2字节）  （不定）                         （1字节）
 *
 * 一个列表可以包含任意多个节点，每一个节点可以保存一个字节数组或者一个整数值
 * 字节数组或整数分别有多种类型可以保存
 *
 * Entry结构：
 * ---------------------------------------------------------------------
 * |       preEntryLength       |      encoding      |     content     |
 * ---------------------------------------------------------------------
 *           ^                           ^                    ^
 *           |                           |                    |
 * 前一节点长度（单位：字节）     content保存的数据类型及长度       节点的值
 *
 * preEntryLength: 长度可以是1字节或5字节
 * 1）前一节点的长度小于127(254一半)字节，preEntryLength属性的长度为1字节：前一节点的长度就保存在这一个字节里面
 * 2）前一节点的长度大于等于127(254一半)字节，preEntryLength长度为5字节：第一字节被设置为0x7F（十进制值127），后四个字节用于保存前一节点的长度
 *
 * encoding:
 * 1）字节数组可以是以下三种长度的其中一种：
 * ---------------------------------------------------------------------------------------------------
 *           content保存的值                                   编码                               编码长度
 * ---------------------------------------------------------------------------------------------------
 * a）长度小于等于63（2^6–1）字节的字节数组:            00bbbbbb                                        1字节
 * b）长度小于等于16383（2^14–1）字节的字节数组:        01bbbbbb_bbbbbbbb                               2字节
 * c）长度小于等于4294967295（2^31–1）字节的字节数组
 *  （比redis中少一半，因为最高位用作来符号位）:         10xxxxxx_bbbbbbbb_bbbbbbbb_bbbbbbbb_bbbbbbbb    5字节
 *
 * 2）整数值则可以是以下六种长度的其中一种（由于java中没有c语言那么多的整数类型，这里略与redis类别不同）：
 * ---------------------------------------------------------------------------------------------------
 *           content保存的值                                   编码                               编码长度
 * ---------------------------------------------------------------------------------------------------
 * a）8位有符号整数:                                         11111110                                1字节
 * b）16位有符号整数:                                        11000000                                1字节
 * c）32位有符号整数:                                        11010000                                1字节
 * d）64位有符号整数:                                        11100000                                1字节
 * @author: huzihan
 * @create: 2021-08-02
 */
public class ZipList {
    // 所有元素以紧凑的方式存储在连续内存中
    private byte [] list;

    public static final int TOTAL_BYTES_LEN = 4;    // 表示整个压缩列表占用字节数，占4个字节长度
    public static final int TAIL_DISTANCE_LEN = 4;  // 表示列表尾距离列表头起始位置距离，占4个字节长度
    public static final int SIZE_LEN = 2;           // 记录列表元素个数，占2个字节，最多能记录2^15次方个元素，因为java中没有无符号数字类型，所有数字类型的正数范围都要除2

    public static final int ZIPLIST_HEADER_SIZE = TOTAL_BYTES_LEN + TAIL_DISTANCE_LEN + SIZE_LEN;   // 列表头占字节长度

    public static final byte END = (byte)0xFF;      // 特殊值（十进制255），用于标记压缩列表的末尾

    public static final byte BIG_PRE_LENGTH_MARK = 0x7F;    // preEntryLength用5个字节存储时的标记

    public static final byte BIG_PRE_LENGTH_SIZE = 5;       // preEntryLength用5个字节存储
    public static final byte SMALL_PRE_LENGTH_SIZE = 1;     // preEntryLength用1个字节存储

    // 字符串编码和整数编码的掩码
    public static final byte INT_MASK = (byte)0b1100_0000;  // 数组的编码头两位00、01、10全都小于11，整数编码前2位全为11

    // 字符串编码类型
    public static final byte STR_6BIT = 0b0000_0000;
    public static final byte STR_14BIT = 0b0100_0000;
    public static final byte STR_32BIT = (byte)0b1000_0000;

    // 整数编码类型
    public static final byte INT_8BIT = (byte)0b1111_1110;
    public static final byte INT_16BIT = (byte)0b1100_0000;
    public static final byte INT_32BIT = (byte)0b1101_0000;
    public static final byte INT_64BIT = (byte)0b1110_0000;

    // 空编码
    public static final byte NONE_ENCODING = (byte)0b1111_1111;

    // 每种类型整数的最大最小值
    public static final byte INT8_MIN = Byte.MIN_VALUE;
    public static final byte INT8_MAX = Byte.MAX_VALUE;
    public static final short INT16_MIN = Short.MIN_VALUE;
    public static final short INT16_MAX = Short.MIN_VALUE;
    public static final int INT32_MIN = Integer.MIN_VALUE;
    public static final int INT32_MAX = Integer.MAX_VALUE;
    public static final long INT64_MIN = Long.MIN_VALUE;
    public static final long INT64_MAX = Long.MAX_VALUE;



    public ZipList() {
        // ZIPLIST_HEADER_SIZE 是 ziplist 表头的大小
        // 1 字节是表末端 ZIP_END 的大小
        int bytes = ZIPLIST_HEADER_SIZE + 1;
        this.list = new byte[bytes];

        // 初始化压缩列表属性
        setBytes(bytes);
        setTailOffset(ZIPLIST_HEADER_SIZE);
        setSize((short)0);

        // 设置表末端
        this.list[bytes - 1] = END;
    }

    /**
     * 静态工厂方法
     * @return
     */
    public static ZipList create() {
        return new ZipList();
    }

    /**
     * 设置压缩列表占用字节数
     * @param totalBytes
     */
    private void setBytes(int totalBytes) {
        // bytes表示压缩列表整体占的字节数，用4个字节表示，正好一个int类型的大小
        byte [] bytes = ByteUtil.intToBytes(totalBytes);
        for (int i = 3; i >= 0; i--) {
            this.list[i] = bytes[i];
        }
    }

    private int getBytes() {
        byte [] bytes = Arrays.copyOfRange(this.list, 0, TOTAL_BYTES_LEN);
        return ByteUtil.bytesToInt(bytes);
    }

    /**
     *
     * 设置链表尾节点距离链表头的字节数
     * @param offset 列表尾节点距离表头有多少字节
     */
    private void setTailOffset(int offset) {
        int tailIndex = TOTAL_BYTES_LEN;
        byte [] bytes = ByteUtil.intToBytes(offset);
        for (int i = 3; i >= 0; i--) {
            this.list[tailIndex + i] = bytes[i];
        }
    }

    /**
     * 获取尾节点距离数组首部的字节数
     * @return
     */
    private int getTailOffset() {
        int tailIndex = TOTAL_BYTES_LEN;
        byte [] bytes = Arrays.copyOfRange(this.list, tailIndex, tailIndex + TAIL_DISTANCE_LEN);
        return ByteUtil.bytesToInt(bytes);
    }

    /**
     * 设置链表中元素个数
     * @param size
     */
    private void setSize(short size) {
        int sizeIndex = TOTAL_BYTES_LEN + TAIL_DISTANCE_LEN;
        byte [] bytes = ByteUtil.shortToBytes(size);
        for (int i = 1; i >= 0; i--) {
            this.list[sizeIndex + i] = bytes[i];
        }
    }

    private short getSize() {
        int sizeIndex = TOTAL_BYTES_LEN + TAIL_DISTANCE_LEN;
        short size = ByteUtil.bytesToShort(Arrays.copyOfRange(this.list, sizeIndex, sizeIndex + SIZE_LEN));
        return size;
    }

    /**
     * 调整列表大小
     * @param size
     */
    private void resize(int size) {
        // 构建一个新size的字节数组
        byte [] newList = new byte[size];
        int copyLength = Math.min(size, this.list.length);
        System.arraycopy(this.list, 0, newList, 0, copyLength);
        this.list = newList;
        // 更新bytes属性
        setBytes(size);
        // 重新设置表末端
        this.list[size - 1] = END;
    }

    public boolean isEmpty() {
        if (this.list[ZIPLIST_HEADER_SIZE] == END) {
            return true;
        }
        return false;
    }

    public void push(byte [] bytes) {
        int position = getTailOffset();
        insert(position, bytes);
    }

    /**
     * 根据 position 所指定的位置，将 bytes数组插入到列表中
     * T = O(N^2)
     * @param position
     * @param bytes
     */
    public void insert(int position, byte [] bytes) {
        ZipListEntry entry = null;

        // 计算新插入节点的preEntryLength属性
        int preEntryLength = 0;
        // 找到被插入元素的preEntryLength
        if (this.list[position] != END) {
            // 不是末尾时
            entry = new ZipListEntry(this.list, position);
            preEntryLength = entry.getPreEntryLength();
        } else {
            // 如果 position 指向表尾末端，那么程序需要检查列表是否为：
            // 1)如果 tail中也指向 END ，那么列表为空；
            // 2)如果列表不为空，那么 tail 将指向列表的最后一个节点。
            int tailPosition = getTailOffset();
            if (this.list[tailPosition] != END) {
                preEntryLength = getEntryLength(this.list, tailPosition);
            }
        }
        int preEntryLengthSize = getEncodePreEntryLengthSize(preEntryLength);

        // 计算当前节点的encoding属性大小和content属性大小
        // 需要对bytes的内容类型做判读：
        // （1）尝试看能否将输入字节数组转换为整数
        int contentSize = 0;
        int encodidngSize = 0;
        byte encoding = NONE_ENCODING;
        if (ByteUtil.tryTransformBytesToLong(bytes)) {
            // bytes数组内的字符能成功转换为数字
            encoding = getIntEncoding(bytes);
            contentSize = getIntContentSize(encoding);
        } else {
            // bytes数组内的字符不能转为数字
            encoding = getStrEncoding(bytes.length);
            contentSize = bytes.length;
        }
        encodidngSize = getEncodingSize(encoding);

        // 计算当前节点占的总字节大小
        int entrySize = preEntryLengthSize + encodidngSize + contentSize;

        // 只要新节点不是被添加到列表末端，
        // 那么程序就需要检查看当前节点的下一个节点的preEntryLength属性的空间是否足够大，能否将entrySize编码到该属性中

        // 编码当前节点长度所要占的字节大小
        int encodingEntrySize = entrySize < BIG_PRE_LENGTH_MARK ? SMALL_PRE_LENGTH_SIZE : BIG_PRE_LENGTH_SIZE;

        // nextdiff 保存了新旧编码之间的字节大小差，如果这个值大于 0
        // 那么说明需要对 p 所指向的节点（的 header ）进行扩展
        // 有可能引起级联更新
        int nextDiff = 0;
        if (bytes[position] != END) {
            int tempDiff = (encodingEntrySize - getPreEntryLengthSize(bytes, position));
            nextDiff =  tempDiff > 0 ? tempDiff : 0;
        };

        // 后一个节点是否是列表的尾节点
        boolean nextIsTail = getTailOffset() == position ? true : false;

        // 扩展空间
        int currentListLength = getBytes();
        resize(currentListLength + entrySize + nextDiff);

        // 必要时移动内存数据并更新tailOffset
        if (this.list[position] != END) {
            // 新元素之后还有节点，因为新元素的加入，需要对这些原有节点进行调整
            // 移动现有元素，为新元素的插入空间腾出位置
            // T = O(N)
            System.arraycopy(this.list, position, this.list, position + entrySize + nextDiff, currentListLength - position);

            // 将新节点的长度编码至后置节点
            setPreEntryLength(this.list, position + entrySize, entrySize);

            // 更新到达表尾的偏移量，将新节点的长度也算上
            // 如果新节点的后面有多于一个节点
            // 那么程序需要将 nextdiff 记录的字节数也计算到表尾偏移量中
            // 这样才能让表尾偏移量正确对齐表尾节点
            if (nextIsTail) {
                setTailOffset(getTailOffset() + entrySize);
            } else {
                setTailOffset(getTailOffset() + entrySize + nextDiff);
            }
        } else {
            // 新元素是尾节点
            setTailOffset(position);
        }

        // 当 nextdiff != 0 时，新节点的后继节点的（header 部分）长度已经被改变，
        // 所以需要级联地更新后续的节点
        if (nextDiff != 0) {
            // T  = O(N^2)
            cascadeUpdate(position + entrySize);
        }

        // 一切搞定，将前置节点的长度写入新节点的 header
        // 将节点值的长度写入新节点的 header
        setPreEntryLength(this.list, position, preEntryLength);
        int contentPosition = position + preEntryLengthSize + encodidngSize;

        // 写入节点
        if (isIntEncoding(encoding)) {
            // 写入编码
            this.list[position + preEntryLengthSize] = encoding;
            // 写入content
            long value = ByteUtil.transformBytesToLong(bytes);
            saveInteger(this.list, contentPosition, value);
        } else {
            // 写入编码
            byte [] encode = encodeStr(bytes.length);
            System.arraycopy(encode, 0, this.list, position + preEntryLength, encode.length);
            // 写入content
            saveStr(this.list, contentPosition, bytes, bytes.length);
        }

        // 更新列表的节点数量计数器
        // T = O(1)值
        setSize((short)(getSize() + 1));
    }

    private void saveInteger(byte [] bytes, int position, long val) {
        if (val >= INT8_MIN && val <= INT8_MAX) {
            bytes[position] = (byte)val;
        } else if (val >= INT16_MIN && val <= INT16_MAX) {
            byte [] shortBytes = ByteUtil.shortToBytes((short)val);
            bytes[position] = shortBytes[0];
            bytes[position + 1] = shortBytes[1];
        } else if (val >= INT32_MIN && val <= INT32_MAX) {
            byte [] shortBytes = ByteUtil.intToBytes((int)val);
            bytes[position] = shortBytes[0];
            bytes[position + 1] = shortBytes[1];
            bytes[position + 2] = shortBytes[2];
            bytes[position + 3] = shortBytes[3];
        } else  {
            byte [] shortBytes = ByteUtil.longToBytes(val);
            bytes[position] = shortBytes[0];
            bytes[position + 1] = shortBytes[1];
            bytes[position + 2] = shortBytes[2];
            bytes[position + 3] = shortBytes[3];
            bytes[position + 4] = shortBytes[4];
            bytes[position + 5] = shortBytes[5];
            bytes[position + 6] = shortBytes[6];
            bytes[position + 7] = shortBytes[7];
        }
    }

    private void saveStr(byte [] bytes, int start, byte [] str, int length) {
        System.arraycopy(str, 0, bytes, start, length);
    }

    /**
     * 当插入新节点时，如果该节点不是在列表尾部，那么，该节点占的字节长度编码可能超出后一个节点preEntryLength属性能编码的空间
     * 为了进行编码，下一个节点在此种情况下也要扩容，而这有可能触发连锁反应造成后续节点逐个扩容
     * 该函数用于实现该级联更新操作
     * @param position 可能引发级联更新的节点的位置
     */
    private void cascadeUpdate(int position) {
        // 指示当前节点位置
        int currentEntryPosition = position;

        while (this.list[currentEntryPosition] != END) {
            // 当前节点占的总字节长度
            int currentEntryLength = getEntryLength(this.list, currentEntryPosition);
            // 下一个节点中的preEntryLength属性如果要记录当前节点字节长度，需要的字节数：1 或 5 ？
            int currentEntryLengthEncodeSize = getEncodePreEntryLengthSize(currentEntryLength);

            // 下一个entry的起始位置
            int nextEntryPosition = currentEntryPosition + currentEntryLength;
            if (this.list[nextEntryPosition] == END) {
                break;
            }
            boolean isNextEntryTail = getTailOffset() == nextEntryPosition ? true : false;

            // 下一个entry中preEntryLength属性实际编码长度
            int nextEntryPreEntryLengthEncodeSize = getPreEntryLengthSize(this.list, nextEntryPosition);

            // 后续节点编码当前节点的空间已经足够，无须再进行任何处理，跳出
            // 可以证明，只要遇到一个空间足够的节点，
            // 那么这个节点之后的所有节点的空间都是足够的
            if (currentEntryLength == getPreEntryLength(this.list, nextEntryPosition)) {
                break;
            }

            // 如果当前节点长度所需的编码长度比下一个节点preEntryLength属性实际编码长度大
            // 则需要扩展空间
            if (currentEntryLengthEncodeSize > nextEntryPreEntryLengthEncodeSize) {
                // 计算差值
                int extra = currentEntryLengthEncodeSize - nextEntryPreEntryLengthEncodeSize;
                // 扩展空间
                int currentListLength = getBytes();
                resize(currentListLength + extra);

                // 后移下个节点及之后的节点数据到合适的位置
                System.arraycopy(this.list, nextEntryPosition, this.list, nextEntryPosition + extra, currentListLength - nextEntryPosition);

                // 更新next节点的preEntryLength属性
                setPreEntryLength(this.list, nextEntryPosition, currentEntryLength);

                // 更新tailOffset属性
                if (isNextEntryTail) {
                    setTailOffset(nextEntryPosition);
                } else {
                    setTailOffset(getTailOffset() + extra);
                }

                currentEntryPosition = nextEntryPosition;
            } else {
                if (currentEntryLengthEncodeSize < nextEntryPreEntryLengthEncodeSize) {
                    // 执行到这里，说明 next 节点编码前置节点的 header 空间有 5 字节
                    // 而编码 rawlen 只需要 1 字节
                    // 但是程序不会对 next 进行缩小，
                    // 所以这里只将 rawlen 写入 5 字节的 header 中就算了。
                    // T = O(1)
                    setPreEntryLengthForceLarge(this.list, nextEntryPosition, currentEntryLength);
                } else {
                    // 运行到这里，
                    // 说明 cur 节点的长度正好可以编码到 next 节点的 header 中
                    // T = O(1)
                    setPreEntryLength(this.list, nextEntryPosition, currentEntryLength);
                }

                break;
            }
        }
     }


    private void setPreEntryLength(byte [] bytes, int position, int length) {
        if (length < BIG_PRE_LENGTH_MARK) {
            bytes[position] = (byte)length;
        } else {
            bytes[position] = BIG_PRE_LENGTH_MARK;
            byte [] intBytes = ByteUtil.intToBytes(length);
            System.arraycopy(intBytes, 0, this.list, position + 1, 4);
        }
    }

    private void setPreEntryLengthForceLarge(byte [] bytes, int position, int length) {
        bytes[position] = BIG_PRE_LENGTH_MARK;
        byte [] intBytes = ByteUtil.intToBytes(length);
        System.arraycopy(intBytes, 0, this.list, position + 1, 4);
    }

    private int getEncodePreEntryLengthSize(int preEntryLength) {
        return preEntryLength < BIG_PRE_LENGTH_MARK ? SMALL_PRE_LENGTH_SIZE : BIG_PRE_LENGTH_SIZE;
    }

    /**
     * 根据给定索引，遍历列表，并返回索引指定节点的位置。
     * 如果索引为正，那么从表头向表尾遍历。
     * 如果索引为负，那么从表尾向表头遍历。
     * 正数索引从 0 开始，负数索引从 -1 开始。
     * 如果索引超过列表的节点数量，或者列表为空，那么返回 -1 。
     * T = O(N)
     * @param index 元素索引
     * @return 元素在byte数组中起始的位置
     */
    public int index(int index) {
        ZipListEntry entry = null;
        int entryIndex = -1;

        if (index < 0) {
            // 负数索引，-1表示从尾节点开始从后向前遍历
            // 将负数转换为正数
            index = (-index) - 1;
            entryIndex = getTailOffset();

            if (!isEmpty()) {
                entry = new ZipListEntry(this.list, entryIndex);
                while (entry.getPreEntryLength() > 0 && index > 0) {
                    entryIndex -= entry.getPreEntryLength();
                    entry = new ZipListEntry(this.list, entryIndex);
                    index--;
                }
            }
        } else {
            entryIndex = ZIPLIST_HEADER_SIZE;
            while (this.list[entryIndex] != END && index > 0) {
                entryIndex += getEntryLength(this.list, entryIndex);
                index--;
            }
        }

        return (this.list[entryIndex] == END || index > 0) ? -1 : entryIndex;
    }

    private byte getIntEncoding(byte [] bytes) {
        if (bytes.length == 0 || bytes.length > 64) {
            throw new IllegalStateException();
        }

        if (ByteUtil.tryTransformBytesToLong(bytes)) {
            long val = ByteUtil.transformBytesToLong(bytes);
            if (val >= INT8_MIN && val <= INT8_MAX) {
                return INT_8BIT;
            } else if (val >= INT16_MIN && val <= INT16_MAX) {
                return INT_16BIT;
            } else if (val >= INT32_MIN && val <= INT32_MAX) {
                return INT_32BIT;
            } else  {
                return INT_64BIT;
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private byte getStrEncoding(int length) {
        if (length <= 63 && length >= 0) {
            return STR_6BIT;
        } else if (length <= 16383) {
            return STR_14BIT;
        } else if (length < Integer.MAX_VALUE) {
            return STR_32BIT;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private byte [] encodeStr(int length) {
        if (length <= 63 && length >= 0) {
            byte [] encoding = new byte[1];
            encoding[0] = (byte)(STR_6BIT | (byte)length);
            return encoding;
        } else if (length <= 16383) {
            byte [] encoding = new byte[2];
            encoding[0] = (byte)(STR_14BIT | (byte)(length >> 8));
            encoding[1] = (byte)length;
            return encoding;
        } else if (length < Integer.MAX_VALUE) {
            byte [] encoding = new byte[5];
            encoding[0] = STR_32BIT;
            byte [] byteLength = ByteUtil.intToBytes(length);
            for (int i = 0; i < 4; i++) {
                encoding[i + 1] = byteLength[i];
            }
            return encoding;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 返回position所指向的节点占用的字节数总和
     * @param bytes
     * @param entryPosition
     * @return
     */
    private int getEntryLength(byte [] bytes, int entryPosition) {
        // 取出编码前置节点的长度所需的字节
        // T = O(1)
        int preEntryLengthSize = getPreEntryLengthSize(bytes, entryPosition);
        // 取出当前节点值的编码类型，编码节点值长度所需的字节数，以及节点值的长度
        // T = O(1)
        int entryEncodingSize = getEncodingSize(bytes, entryPosition);
        int entryContentSize = getContentSize(bytes, entryPosition);
        // 计算节点占用的字节数总和
        return preEntryLengthSize + entryEncodingSize + entryContentSize;
    }

    /**
     * 解析编码前置节点长度所需的字节数
     * @param bytes
     * @param entryPosition
     * @return
     */
    private int getPreEntryLengthSize(byte [] bytes, int entryPosition) {
        if (bytes[entryPosition] < BIG_PRE_LENGTH_MARK) {
            return SMALL_PRE_LENGTH_SIZE;
        } else {
            return BIG_PRE_LENGTH_SIZE;
        }
    }

    /**
     * 获取节点中记录的preEntryLength属性值
     * @param bytes
     * @param entryPosition
     * @return
     */
    private int getPreEntryLength(byte [] bytes, int entryPosition) {
        int preEntryLength = 0;
        if (bytes[entryPosition] == BIG_PRE_LENGTH_MARK) {
            preEntryLength = ByteUtil.bytesToInt(Arrays.copyOfRange(bytes, entryPosition + 1, entryPosition + 5));
        } else {
            preEntryLength = (int)bytes[entryPosition];
        }
        return preEntryLength;
    }


    /**
     * 计算entry中content部分所占的字节数
     * @param bytes
     * @param entryPosition
     * @return
     */
    private int getContentSize(byte [] bytes, int entryPosition) {
        byte encoding = getEntryEncoding(bytes, entryPosition);
        int contentSize = 0;
        if (isIntEncoding(encoding)) {
            // 整数编码
            contentSize = getIntContentSize(encoding);
        } else {
            // 字符串
            contentSize = getStrContentSize(bytes, entryPosition);
        }
        return contentSize;
    }

    /**
     * 计算entry中encoding部分占的字节数
     * @param bytes
     * @param entryPosition
     * @return
     */
    private int getEncodingSize(byte [] bytes, int entryPosition) {
        byte encoding = getEntryEncoding(bytes, entryPosition);
        int encodingSize = 0;
        if (isIntEncoding(encoding)) {
            encodingSize = 1;
        } else {
            if (encoding == STR_6BIT) {
                encodingSize = 1;
            } else if (encoding == STR_14BIT) {
                encodingSize = 2;
            } else if (encoding == STR_32BIT) {
                encodingSize = 5;
            } else {
                throw new IllegalStateException();
            }
        }

        return encodingSize;
    }

    private int getEncodingSize(byte encoding) {
        switch (encoding) {
            case INT_8BIT:
                return 1;
            case INT_16BIT:
                return 1;
            case INT_32BIT:
                return 1;
            case INT_64BIT:
                return 1;
            case STR_6BIT:
                return 1;
            case STR_14BIT:
                return 2;
            case STR_32BIT:
                return 5;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * 根据encoding内容判断是否是整数编码
     * @param encoding
     * @return
     */
    private boolean isIntEncoding(byte encoding) {
        if ((encoding & INT_MASK) == INT_MASK) {
            // 整数类型编码
            return true;
        } else {
            return false;
        }
    }

    private int encodingContent() {
        return 1;
    }

    /**
     * 获取entry中encoding属性的字节下标位置
     * @param bytes
     * @param entryPosition
     * @return
     */
    private int getEntryEncodingPosition(byte [] bytes, int entryPosition) {
        int preEntryLengthSize = getPreEntryLengthSize(bytes, entryPosition);
        int encodingPosition = entryPosition + preEntryLengthSize;
        return encodingPosition;
    }

    /**
     * 获取entry的content的编码
     * @param bytes
     * @param entryPosition
     * @return
     */
    private byte getEntryEncoding(byte [] bytes, int entryPosition) {
        int preEntryLengthSize = getPreEntryLengthSize(bytes, entryPosition);
        byte encodingByte = bytes[entryPosition + preEntryLengthSize];

        // 判断是数组编码还是整数编码
        if (!isIntEncoding(encodingByte)) {
            // 为数组
            // 数组编码只有前2位用于标识类别
            encodingByte = (byte)(encodingByte & INT_MASK);
        }
        return encodingByte;
    }

    /**
     * 获取整数content占的字节数量
     * @param encoding
     * @return
     */
    private int getIntContentSize(byte encoding) {
        switch (encoding) {
            case INT_8BIT:
                return 1;
            case INT_16BIT:
                return 2;
            case INT_32BIT:
                return 4;
            case INT_64BIT:
                return 8;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * 获取entry中字符串的content占的字节数
     * @param bytes
     * @param entryPosition
     * @return
     */
    private int getStrContentSize(byte [] bytes, int entryPosition) {
        int encodingIndex = getEntryEncodingPosition(bytes, entryPosition);
        int contentSize = 0;
        byte rawEncodingByte = bytes[encodingIndex];
        switch (rawEncodingByte) {
            case STR_6BIT:
                contentSize = rawEncodingByte & 0x3F;
                break;
            case STR_14BIT:
                byte byte1 = (byte)(rawEncodingByte & 0x3F);
                byte byte2 = bytes[encodingIndex + 1];
                contentSize = ByteUtil.bytesToShort(new byte[] {byte1, byte2});
                break;
            case STR_32BIT:
                // 注意：这里得到的int结果是有符号的，及最高位是作为符号位使用的，而不是redis中的无符号数
                contentSize = ByteUtil.bytesToInt(new byte[] {
                    bytes[encodingIndex + 1],
                    bytes[encodingIndex + 2],
                    bytes[encodingIndex + 3],
                    bytes[encodingIndex + 4]
                });
                break;
            default:
                throw new IllegalStateException();
        }

        return contentSize;
    }

    public int find(int start, Object object) {
        int position = ZIPLIST_HEADER_SIZE;
        int count = 0;
        while (this.list[position] != END && count < start) {
            ZipListEntry entry = new ZipListEntry(this.list, position);
            int entryLength = entry.getEntryLength();
            position += entryLength;
            count++;
        }

        int length = getBytes();
        while (position < length && this.list[position] != END) {
            ZipListEntry entry = new ZipListEntry(this.list, position);
            int entryLength = entry.getEntryLength();
            
        }

        return 1;
    }

    public Object next() {
        return null;
    }

    public Object get() {
        return null;
    }


    public void delete() {

    }

    public int length() {
        return getSize();
    }

    public int blobLength() {
        return getBytes();
    }

    /**
     * 保存ZipList节点信息的结构
     */
    public static class ZipListEntry {

        int preEntryLength;     // 前置节点的长度
        int preEntryLengthSize; // 编码preEntreyLength所需的字节大小
        int entryLength;        // 当前节点值的长度
        int entryLengthSize;    // 编码entryLength所需的字节大小
        byte encoding;          // 当前节点值所使用的编码类型
        int encodingSize;       // 当前节点encoding的大小
        int contentSize;        // 节点content大小
        int position;           // 指向当前节点位置

        public ZipListEntry(byte [] bytes, int position) {
            this.position = position;

            if (bytes[position] == BIG_PRE_LENGTH_MARK) {
                this.preEntryLength = ByteUtil.bytesToInt(Arrays.copyOfRange(bytes, position + 1, position + 5));
                this.preEntryLengthSize = BIG_PRE_LENGTH_SIZE;
            } else {
                this.preEntryLength = (int)bytes[position];
                this.preEntryLengthSize = SMALL_PRE_LENGTH_SIZE;
            }

            int encodingPosition = position + this.preEntryLengthSize;
            byte firstEncoding = bytes[encodingPosition];

            if ((firstEncoding & INT_MASK) == INT_MASK) {
                // 整数类型编码
                this.encoding = firstEncoding;

            } else {
                this.encoding = (byte)(firstEncoding & INT_MASK);
            }

            switch (this.encoding) {
                case INT_8BIT:
                    this.encodingSize  = 1;
                    break;
                case INT_16BIT:
                    this.encodingSize  = 1;
                    break;
                case INT_32BIT:
                    this.encodingSize  = 1;
                    break;
                case INT_64BIT:
                    this.encodingSize  = 1;
                    break;
                case STR_6BIT:
                    this.encodingSize  = 1;
                    break;
                case STR_14BIT:
                    this.encodingSize  = 2;
                    break;
                case STR_32BIT:
                    this.encodingSize  = 5;
                    break;
                default:
                    throw new IllegalStateException();
            }

            if ((firstEncoding & INT_MASK) == INT_MASK) {
                // 整数类型编码
                switch (this.encoding) {
                    case INT_8BIT:
                        this.contentSize = 1;
                        break;
                    case INT_16BIT:
                        this.contentSize = 2;
                        break;
                    case INT_32BIT:
                        this.contentSize = 4;
                        break;
                    case INT_64BIT:
                        this.contentSize = 8;
                    default:
                        throw new IllegalStateException();
                }
            } else {
                switch (this.encoding) {
                    case STR_6BIT:
                        this.contentSize = firstEncoding & 0x3F;
                        break;
                    case STR_14BIT:
                        byte byte1 = (byte)(firstEncoding & 0x3F);
                        byte byte2 = bytes[encodingPosition + 1];
                        this.contentSize = ByteUtil.bytesToShort(new byte[] {byte1, byte2});
                        break;
                    case STR_32BIT:
                        // 注意：这里得到的int结果是有符号的，及最高位是作为符号位使用的，而不是redis中的无符号数
                        this.contentSize = ByteUtil.bytesToInt(new byte[] {
                                bytes[encodingPosition + 1],
                                bytes[encodingPosition + 2],
                                bytes[encodingPosition + 3],
                                bytes[encodingPosition + 4]
                        });
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }

            this.entryLength = this.preEntryLength + this.encodingSize + this.contentSize;
            this.entryLengthSize = this.entryLength < BIG_PRE_LENGTH_MARK ? SMALL_PRE_LENGTH_SIZE : BIG_PRE_LENGTH_SIZE;
        }

        public int getPreEntryLength() {
            return this.preEntryLength;
        }

        public int getEntryLength() {
            return this.entryLength;
        }
    }

}
