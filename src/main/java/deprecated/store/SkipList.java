package deprecated.store;

import java.text.DecimalFormat;

/**
 * 跳跃表数据结构
 *
 * @author: huzihan
 * @create: 2021-07-16
 */
public class SkipList {
    private SkipListNode header;
    private SkipListNode tail;
    private int level;
    private int length;

    private static final double SKIPLIST_P = 0.5;       // 随机层高每升高一层的概率
    private static final int SKIPLIST_MAX_LEVEL = 32;   // SkipList最大层高

    /**
     * 私有化构造函数，所有创建SkipList都通过工厂方法来进行
     */
    private SkipList() {
        this.header = new SkipListNode(0, null, SKIPLIST_MAX_LEVEL);
        this.tail = null;
        this.level = 1;
        this.length = 0;
    }

    /**
     * 用于创建SkipList的工厂方法
     *
     * @return 一个新的SkipList实例对象
     */
    public static SkipList createSkipList() {
        return new SkipList();
    }

    /**
     * 向SkipList中添加（score，obj）的新节点
     *
     * @param score 分值
     * @param obj   对象
     * @return      创建的新节点引用
     */
    public SkipListNode insert(double score, Object obj) throws IllegalArgumentException{

        if (obj == null) {
            throw new IllegalArgumentException("obj in SkipListNode can't be null.");
        }

        SkipListNode [] nodesToUpdate = new SkipListNode[SKIPLIST_MAX_LEVEL];   // 用于记录每一层需要修改的节点（即新节点会插入在这些节点之后）
        int [] accRankByLevel = new int[SKIPLIST_MAX_LEVEL];                    // 记录每一层待修改节点的总跨度

        SkipListNode currentNode = this.header;

        // 搜索跳跃表各个节点的各层级引用，找到新节点会被插入到每层的哪个节点之后，记录到对应的nodesToUpdate上
        // 同时，记录每层被修改节点相对于header的累计跨度
        for (int currentLevel = this.level - 1; currentLevel >= 0; currentLevel--) {

            // 对每一层，初始化改层的累计跨度：是从0开始/还是继承自上一层的累计值
            accRankByLevel[currentLevel] = (currentLevel == this.level - 1) ? 0 : accRankByLevel[currentLevel + 1];

            // 在同一层内一直向后遍历，直到找到新节点要插入的那个节点的位置
            // warning: 这里有一个待实现功能：原redis在score相同时，会比较obj的字典序，这里暂时未实现。
            while (currentNode.level[currentLevel].forward != null
                    && currentNode.level[currentLevel].forward.score < score) {
                accRankByLevel[currentLevel] += currentNode.level[currentLevel].span;
                currentNode = currentNode.level[currentLevel].forward;
            }

            nodesToUpdate[currentLevel] = currentNode;
        }

        // 创建新节点实例
        int newNodeLevel = getRandomLevel();
        SkipListNode newNode = new SkipListNode(score, obj, newNodeLevel);

        // 如果新节点的层高比现有SkipList层高要大，则相应层的nodeToUpdate和accRankByLevel也会被用到，要进行初始化
        if (this.level < newNodeLevel) {
            for (int l = newNodeLevel - 1; l >= this.level; l--) {
                nodesToUpdate[l] = this.header;
                nodesToUpdate[l].level[l].span = this.length;
                accRankByLevel[l] = 0;
            }

            this.level = newNodeLevel;
        }

        // 插入新节点，更新被插入位置的节点的forward指针和跨度
        for (int currentLevel = 0; currentLevel < newNodeLevel; currentLevel++) {
            newNode.level[currentLevel].forward = nodesToUpdate[currentLevel].level[currentLevel].forward;
            nodesToUpdate[currentLevel].level[currentLevel].forward = newNode;

            newNode.level[currentLevel].span = accRankByLevel[currentLevel] + nodesToUpdate[currentLevel].level[currentLevel].span - accRankByLevel[0];
            nodesToUpdate[currentLevel].level[currentLevel].span = accRankByLevel[0] - accRankByLevel[currentLevel] + 1;
        }

        // 如果新节点的层高不比现有SkipList层高大，则这些更高层的跨度由于新节点的插入要加一
        for (int currentLevel = newNodeLevel; currentLevel < this.level; currentLevel++) {
            nodesToUpdate[currentLevel].level[currentLevel].span++;
        }

        // 更新新节点和其后续节点的backward指针
        newNode.backward = (nodesToUpdate[0] == this.header) ? null : nodesToUpdate[0];
        if (newNode.level[0].forward == null) {
            this.tail = newNode;    // 更新尾指针
        } else {
            newNode.level[0].forward.backward = newNode;
        }

        // 更新SkipList长度
        this.length++;

        return newNode;
    }

    /**
     * 删除节点（score，obj）
     *
     * @param score
     * @param obj
     * @return 操作是否成功
     */
    public boolean delete(double score, Object obj) {
        // 用于记录每一层需要修改的节点（即新节点会插入在这些节点之后）
        SkipListNode [] nodesToUpdate = new SkipListNode[SKIPLIST_MAX_LEVEL];

        // 从header开始查找对应节点的位置
        SkipListNode currentNode = this.header;
        for (int currentLevel = this.level - 1; currentLevel >= 0; currentLevel--) {
            while (currentNode.level[currentLevel].forward != null
                    && currentNode.level[currentLevel].forward.score < score) {
                currentNode = currentNode.level[currentLevel].forward;
            }

            nodesToUpdate[currentLevel] = currentNode;
        }

        // 判断找到的位置之后是否就是想要删除的节点
        if ( ! (nodesToUpdate[0].level[0].forward.score == score
                && (obj == nodesToUpdate[0].level[0].forward.obj)
                || obj.equals(nodesToUpdate[0].level[0].forward.obj))) {
            return false;
        }

        // 待删除的节点
        SkipListNode nodeToDelete = nodesToUpdate[0].level[0].forward;

        // 更新对应每层的指针和跨度
        for (int currentLevel = 0; currentLevel < this.level; currentLevel++) {
            if (nodesToUpdate[currentLevel].level[currentLevel].forward == nodeToDelete) {
                nodesToUpdate[currentLevel].level[currentLevel].span += nodeToDelete.level[currentLevel].span - 1;
                nodesToUpdate[currentLevel].level[currentLevel].forward = nodeToDelete.level[currentLevel].forward;
            } else {
                nodesToUpdate[currentLevel].level[currentLevel].span -= 1;
            }
        }

        // 更新节点的回退指针
        if (nodeToDelete.level[0].forward != null) {
            nodeToDelete.level[0].forward.backward = nodeToDelete.backward;
        } else {
            this.tail = nodeToDelete.backward;
        }

        // 重写调整SkipList的最大层高
        while (this.level > 1 && this.header.level[this.level - 1].forward == null) {
            this.header.level[this.level - 1].span = 0;
            this.level--;
        }

        // 更新SkipList长度
        this.length--;

        return true;
    }


    /**
     * 返回包含给定成员和分值的节点在跳跃表中的排位
     * @param score
     * @param obj
     * @return
     */
    public int getRank(double score, Object obj) {
        SkipListNode currentNode = this.header;
        int rank = 0;

        for (int currentLevel = this.level - 1; currentLevel >= 0; currentLevel--) {
            while (currentNode.level[currentLevel].forward != null
                    && currentNode.level[currentLevel].forward.score < score) {
                rank += currentNode.level[currentLevel].span;
                currentNode = currentNode.level[currentLevel].forward;
            }

            if (currentNode.level[currentLevel].forward != null
                    && currentNode.level[currentLevel].forward.score == score
                    && currentNode.level[currentLevel].forward.obj.equals(obj)) {

                rank += currentNode.level[currentLevel].span;

                return rank;
            }
        }

        // 没找到
        return 0;
    }


    /**
     * 返回跳跃表在给定排位上的节点，若给定排位无节点，返回null
     * @param rank
     * @return 给定排位上的节点
     */
    public SkipListNode getElementByRank(int rank) {
        if (rank > this.length) {
            return null;
        } else if (rank == this.length) {
            return this.tail;
        }

        SkipListNode currentNode = this.header;
        int accRank = 0;

        SkipListNode target = null;

        for (int currentLevel = this.level - 1; currentLevel >= 0; currentLevel--) {
            while (currentNode.level[currentLevel].span < rank - accRank) {
                accRank += currentNode.level[currentLevel].span;
                currentNode = currentNode.level[currentLevel].forward;
            }

            if (currentNode.level[currentLevel].span == rank - accRank) {
                target = currentNode.level[currentLevel].forward;
                break;
            }
        }

        return target;
    }

    /**
     * 如果给定的分值范围包含在跳跃表的分值范围内，则返回true，否则返回false
     * @param range 表示范围
     * @return
     */
    public boolean isInRank(ScoreRangeSpec range) {
        if (range.getMax() > range.getMax()
            || (range.getMin() == range.getMax()
                && (!range.isMinExist() || !range.isMaxExist()))) {
            return false;
        }

        SkipListNode maxNode = this.tail;

        if (maxNode == null) {
            return false;
        }

        boolean outOfMinRange = range.isMinExist() ? maxNode.score < range.getMin() : maxNode.score <= range.getMin();
        if (outOfMinRange) {
            return false;
        }

        SkipListNode minNode = this.header.level[0].forward;

        boolean outOfMaxRange = range.isMaxExist() ? minNode.score > range.getMax() : minNode.score >= range.getMax();
        if (outOfMaxRange) {
            return false;
        }

        return true;
    }


    /**
     * 给定一个分值范围，返回第一个符合该范围的节点
     *
     * @param range 分值范围
     * @return 第一个符合该范围的节点，如果没有节点的值在给定范围内，返回null
     */
    public SkipListNode getFirstInRank(ScoreRangeSpec range) {
        if (!isInRank(range)) {
            return null;
        }

        SkipListNode currentNode = this.header;
        for (int currentLevel = this.level - 1; currentLevel >= 0; currentLevel--) {
            while (currentNode.level[currentLevel].forward != null) {
                if (range.isMinExist()) {
                    if (currentNode.level[currentLevel].forward.score < range.getMin()) {
                        currentNode = currentNode.level[currentLevel].forward;
                    } else {
                        break;
                    }
                } else {
                    if (currentNode.level[currentLevel].forward.score <= range.getMin()) {
                        currentNode = currentNode.level[currentLevel].forward;
                    } else {
                        break;
                    }
                }
            }
        }

        SkipListNode targetNode = currentNode.level[0].forward;

        return targetNode;
    }

    /**
     * 给定一个分值范围，返回最后一个符合该范围的节点
     *
     * @param range 分值范围
     * @return 最后一个符合该范围的节点，如果没有节点的值在给定范围内，返回null
     */
    public SkipListNode getLastInRank(ScoreRangeSpec range) {
        if (!isInRank(range)) {
            return null;
        }

        SkipListNode currentNode = this.header;
        for (int currentLevel = this.level - 1; currentLevel >= 0; currentLevel--) {
            while (currentNode.level[currentLevel].forward != null) {
                if (range.isMaxExist()) {
                    if (currentNode.level[currentLevel].forward.score <= range.getMax()) {
                        currentNode = currentNode.level[currentLevel].forward;
                    } else {
                        break;
                    }
                } else {
                    if (currentNode.level[currentLevel].forward.score < range.getMax()) {
                        currentNode = currentNode.level[currentLevel].forward;
                    } else {
                        break;
                    }
                }
            }
        }

        return currentNode;
    }

    /**
     * 给定一个分值范围，删除跳跃表中所有在这个范围内的节点
     * redis实现中会同步删除dict中的节点，但我认为这个应该单独分开操作，而不是放在SkipList的逻辑里
     * @param range 分值范围
     * @return 被删除的节点数量
     */
    public long deleteRangeByScore(ScoreRangeSpec range) {
        SkipListNode currentNode = this.header;
        for (int currentLevel = this.level - 1; currentLevel >= 0; currentLevel--) {
            while (currentNode.level[currentLevel].forward != null
                    && (range.isMinExist()
                        ? currentNode.level[currentLevel].forward.score < range.getMin()
                        : currentNode.level[currentLevel].forward.score <= range.getMin())) {
                currentNode = currentNode.level[currentLevel].forward;
            }
        }

        currentNode = currentNode.level[0].forward;
        SkipListNode next = null;

        int removedCount = 0;
        while (currentNode != null
                && (range.isMaxExist()
                    ? currentNode.score <= range.getMax()
                    : currentNode.score < range.getMax())) {
            next = currentNode.level[0].forward;
            delete(currentNode.score, currentNode.obj);
            currentNode = next;

            removedCount++;
        }

        return removedCount;
    }

    /**
     * 给定一个排位范围，删除跳跃表中在这个排位范围内的节点
     * @param start 起始排位，包含该位置的元素（闭区间），从1开始排位
     * @param end 结束排位，包含该位置的元素（闭区间）
     * @return 删除的节点数量
     */
    public long deleteRangeByRank(int start, int end) {
        SkipListNode currentNode = this.header;
        int accRank = 0;

        for (int currentLevel = this.level - 1; currentLevel >= 0; currentLevel--) {
            while (currentNode.level[currentLevel].forward != null
                    && accRank + currentNode.level[currentLevel].span < start) {
                accRank += currentNode.level[currentLevel].span;
                currentNode = currentNode.level[currentLevel].forward;
            }
        }

        currentNode = currentNode.level[0].forward;
        accRank++;

        long removeCount = 0;
        while (currentNode != null && accRank <= end) {
            SkipListNode next = currentNode.level[0].forward;
            delete(currentNode.score, currentNode.obj);
            removeCount++;
            currentNode = next;
            accRank++;
        }

        return removeCount;
    }

    /**
     * SkipList的节点
     */
    public static class SkipListNode {
        private double score;
        private Object obj;

        private SkipListLevel [] level;

        private SkipListNode backward;

        public SkipListNode(double score, Object obj, int level) {
            this.score = score;
            this.obj = obj;
            this.level = new SkipListLevel[level];
            for (int i = 0; i < level; i++) {
                this.level[i] = new SkipListLevel(null, 0);
            }

            this.backward = null;
        }

        public double getScore() {
            return this.score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public Object getObj() {
            return this.obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }

        public SkipListNode getBackward() {
            return this.backward;
        }

        public void setBackward(SkipListNode backward) {
            this.backward = backward;
        }
    }

    /**
     * 用于表示单个SkipListNode里的“层”，记录了改层指向的下一个节点，以及起跨度
     * 原始跳跃表没有span这个属性，这是为了方便实现有序列表排序功能而添加的属性
     */
    private static class SkipListLevel {
        private SkipListNode forward;
        private int span;

        public SkipListLevel(SkipListNode forward, int span) {
            this.forward = forward;
            this.span = span;
        }

        public SkipListNode getForward() {
            return this.forward;
        }

        public int getSpan() {
            return this.span;
        }

        public void setForward(SkipListNode forward) {
            this.forward = forward;
        }

        public void setSpan(int span) {
            this.span = span;
        }
    }

    /**
     * 根据幂次定律，在创建新SkipListNode时，随机生成新节点的层高，越高的层高出现的概览越小。
     * 改随机方法生成1的概览为1/2，生成2的概览为1/4，生成3的概览为1/8，以此类推。
     *
     * @return 节点随机层高
     */
    private int getRandomLevel() {
        int level = 1;
        while (Math.random() < SKIPLIST_P && level < SKIPLIST_MAX_LEVEL) {
            level++;
        }

        return level;
    }

    /**
     * 表示分值范围的结构
     */
    public static class ScoreRangeSpec {
        private double min, max;            // 最小值和最大值
        private boolean minExist, maxExist; // 左闭包和右闭包是否存在

        public ScoreRangeSpec(boolean minExist, double min, boolean maxExist, double max) {
            this.minExist = minExist;
            this.maxExist = maxExist;
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public boolean isMinExist() {
            return minExist;
        }

        public boolean isMaxExist() {
            return maxExist;
        }
    }

    /**
     * 用于测试：显示SkipList的结构
     */
    public void showStructure() {
        DecimalFormat df=new DecimalFormat("00.0");

        for (int i = SKIPLIST_MAX_LEVEL - 1; i >= 0; i--) {

            StringBuilder lineStructure = new StringBuilder();

            SkipListNode node = this.header;

            while (node != null) {
                lineStructure.append(df.format(node.score));

                for (int j = 0; j < node.level[i].span - 1; j++) {
                    lineStructure.append("----");
                }


                for (int j = 0; j < node.level[i].span; j++) {
                    lineStructure.append("----");
                }


                node = node.level[i].forward;
            }
            System.out.println(lineStructure.toString());
        }
    }
}
