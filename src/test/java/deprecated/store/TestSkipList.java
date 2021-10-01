package deprecated.store;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-06-30
 */
public class TestSkipList {
    @Test
    public void testInsert() {
        SkipList instance = SkipList.createSkipList();
        String str = "a";

        for (int i = 30; i > 0; i--) {
            instance.insert(i, str);
        }

        instance.showStructure();


        for (int i = 30; i > 0; i--) {
            instance.delete(i, str);
        }
    }

    @Test
    public void testDelete() {
        SkipList instance = SkipList.createSkipList();
        String str = "a";

        for (int i = 10; i > 0; i--) {
            instance.insert(i, str);
        }

        instance.showStructure();

        instance.delete(10.0, str);
        instance.delete(5.0, str);
        instance.delete(2.0, str);
        instance.delete(7.0, str);

        instance.showStructure();


        instance.delete(1.0, str);
        instance.delete(3.0, str);
        instance.delete(4.0, str);
        instance.delete(6.0, str);
        instance.delete(8.0, str);
        instance.delete(9.0, str);
    }

    @Test
    public void testGetElementByRank() {
        SkipList instance = SkipList.createSkipList();
        String str = "a";

        for (int i = 10; i > 0; i--) {
            instance.insert(i, str);
        }

        SkipList.SkipListNode target = instance.getElementByRank(0);
        Assert.assertEquals(null, target);

        target = instance.getElementByRank(1);
        Assert.assertEquals(1.0, target.getScore());

        target = instance.getElementByRank(6);
        Assert.assertEquals(6.0, target.getScore());

        target = instance.getElementByRank(10);
        Assert.assertEquals(10.0, target.getScore());

        target = instance.getElementByRank(11);
        Assert.assertEquals(null, target);

        for (int i = 10; i > 0; i--) {
            instance.delete(i, str);
        }

    }

    @Test
    public void TestIsInRange() {
        SkipList instance = SkipList.createSkipList();
        String str = "a";
        for (int i = 5; i <= 30; i++) {
            instance.insert(i, str);
        }

        SkipList.ScoreRangeSpec range1 = new SkipList.ScoreRangeSpec(true, 10, true, 20);
        SkipList.ScoreRangeSpec range2 = new SkipList.ScoreRangeSpec(false, 10, false, 20);
        SkipList.ScoreRangeSpec range3 = new SkipList.ScoreRangeSpec(true, 10, false, 20);
        SkipList.ScoreRangeSpec range4 = new SkipList.ScoreRangeSpec(false, 10, true, 20);

        boolean ans1 = instance.isInRank(range1);
        boolean ans2 = instance.isInRank(range2);
        boolean ans3 = instance.isInRank(range3);
        boolean ans4 = instance.isInRank(range4);

        Assert.assertEquals(true, ans1);
        Assert.assertEquals(true, ans2);
        Assert.assertEquals(true, ans3);
        Assert.assertEquals(true, ans4);

        SkipList.ScoreRangeSpec range5 = new SkipList.ScoreRangeSpec(true, 1, true, 50);
        SkipList.ScoreRangeSpec range6 = new SkipList.ScoreRangeSpec(false, 1, false, 50);
        SkipList.ScoreRangeSpec range7 = new SkipList.ScoreRangeSpec(true, 1, false, 50);
        SkipList.ScoreRangeSpec range8 = new SkipList.ScoreRangeSpec(false, 1, true, 50);

        boolean ans5 = instance.isInRank(range5);
        boolean ans6 = instance.isInRank(range6);
        boolean ans7 = instance.isInRank(range7);
        boolean ans8 = instance.isInRank(range8);

        Assert.assertEquals(true, ans5);
        Assert.assertEquals(true, ans6);
        Assert.assertEquals(true, ans7);
        Assert.assertEquals(true, ans8);


        SkipList.ScoreRangeSpec range9 = new SkipList.ScoreRangeSpec(true, 1, true, 5);
        SkipList.ScoreRangeSpec range10 = new SkipList.ScoreRangeSpec(false, 1, false, 5);
        SkipList.ScoreRangeSpec range11 = new SkipList.ScoreRangeSpec(true, 1, false, 5);
        SkipList.ScoreRangeSpec range12 = new SkipList.ScoreRangeSpec(false, 1, true, 5);

        boolean ans9 = instance.isInRank(range9);
        boolean ans10 = instance.isInRank(range10);
        boolean ans11 = instance.isInRank(range11);
        boolean ans12 = instance.isInRank(range12);

        Assert.assertEquals(true, ans9);
        Assert.assertEquals(false, ans10);
        Assert.assertEquals(false, ans11);
        Assert.assertEquals(true, ans12);


        SkipList.ScoreRangeSpec range13 = new SkipList.ScoreRangeSpec(true, 30, true, 35);
        SkipList.ScoreRangeSpec range14 = new SkipList.ScoreRangeSpec(false, 30, false, 35);
        SkipList.ScoreRangeSpec range15 = new SkipList.ScoreRangeSpec(true, 30, false, 35);
        SkipList.ScoreRangeSpec range16 = new SkipList.ScoreRangeSpec(false, 30, true, 35);

        boolean ans13 = instance.isInRank(range13);
        boolean ans14 = instance.isInRank(range14);
        boolean ans15 = instance.isInRank(range15);
        boolean ans16 = instance.isInRank(range16);

        Assert.assertEquals(true, ans13);
        Assert.assertEquals(false, ans14);
        Assert.assertEquals(true, ans15);
        Assert.assertEquals(false, ans16);

        for (int i = 5; i < 30; i++) {
            instance.delete(i, str);
        }
    }

    @Test
    public void testGetFirstInRange() {
        SkipList instance = SkipList.createSkipList();
        String str = "a";
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        SkipList.ScoreRangeSpec range1 = new SkipList.ScoreRangeSpec(true, 5, true, 20);
        SkipList.ScoreRangeSpec range2 = new SkipList.ScoreRangeSpec(false, 5, true, 20);
        SkipList.ScoreRangeSpec range3 = new SkipList.ScoreRangeSpec(true, 1, true, 20);
        SkipList.ScoreRangeSpec range4 = new SkipList.ScoreRangeSpec(false, 1, true, 20);
        SkipList.ScoreRangeSpec range5 = new SkipList.ScoreRangeSpec(true, 1, true, 5);
        SkipList.ScoreRangeSpec range6 = new SkipList.ScoreRangeSpec(true, 10, true, 20);
        SkipList.ScoreRangeSpec range7 = new SkipList.ScoreRangeSpec(false, 10, true, 20);
        SkipList.ScoreRangeSpec range8 = new SkipList.ScoreRangeSpec(true, 30, true, 40);
        SkipList.ScoreRangeSpec range9 = new SkipList.ScoreRangeSpec(true, 1, true, 4);

        SkipList.SkipListNode ans1 = instance.getFirstInRank(range1);
        SkipList.SkipListNode ans2 = instance.getFirstInRank(range2);
        SkipList.SkipListNode ans3 = instance.getFirstInRank(range3);
        SkipList.SkipListNode ans4 = instance.getFirstInRank(range4);
        SkipList.SkipListNode ans5 = instance.getFirstInRank(range5);
        SkipList.SkipListNode ans6 = instance.getFirstInRank(range6);
        SkipList.SkipListNode ans7 = instance.getFirstInRank(range7);
        SkipList.SkipListNode ans8 = instance.getFirstInRank(range8);
        SkipList.SkipListNode ans9 = instance.getFirstInRank(range9);

        Assert.assertEquals(5.0, ans1.getScore());
        Assert.assertEquals(6.0, ans2.getScore());
        Assert.assertEquals(5.0, ans3.getScore());
        Assert.assertEquals(5.0, ans4.getScore());
        Assert.assertEquals(5.0, ans5.getScore());
        Assert.assertEquals(10.0, ans6.getScore());
        Assert.assertEquals(11.0, ans7.getScore());
        Assert.assertEquals(null, ans8);
        Assert.assertEquals(null, ans9);

        for (int i = 5; i <= 20; i++) {
            instance.delete(i, str);
        }
    }

    @Test
    public void testGetLastInRange() {
        SkipList instance = SkipList.createSkipList();
        String str = "a";
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        SkipList.ScoreRangeSpec range1 = new SkipList.ScoreRangeSpec(true, 5, true, 20);
        SkipList.ScoreRangeSpec range2 = new SkipList.ScoreRangeSpec(true, 5, false, 20);
        SkipList.ScoreRangeSpec range3 = new SkipList.ScoreRangeSpec(true, 5, true, 25);
        SkipList.ScoreRangeSpec range4 = new SkipList.ScoreRangeSpec(true, 5, false, 25);
        SkipList.ScoreRangeSpec range5 = new SkipList.ScoreRangeSpec(true, 5, true, 15);
        SkipList.ScoreRangeSpec range6 = new SkipList.ScoreRangeSpec(true, 5, false, 15);
        SkipList.ScoreRangeSpec range7 = new SkipList.ScoreRangeSpec(true, 1, true, 5);
        SkipList.ScoreRangeSpec range8 = new SkipList.ScoreRangeSpec(true, 30, true, 40);
        SkipList.ScoreRangeSpec range9 = new SkipList.ScoreRangeSpec(true, 1, true, 4);

        SkipList.SkipListNode ans1 = instance.getLastInRank(range1);
        SkipList.SkipListNode ans2 = instance.getLastInRank(range2);
        SkipList.SkipListNode ans3 = instance.getLastInRank(range3);
        SkipList.SkipListNode ans4 = instance.getLastInRank(range4);
        SkipList.SkipListNode ans5 = instance.getLastInRank(range5);
        SkipList.SkipListNode ans6 = instance.getLastInRank(range6);
        SkipList.SkipListNode ans7 = instance.getLastInRank(range7);
        SkipList.SkipListNode ans8 = instance.getLastInRank(range8);
        SkipList.SkipListNode ans9 = instance.getLastInRank(range9);

        Assert.assertEquals(20.0, ans1.getScore());
        Assert.assertEquals(19.0, ans2.getScore());
        Assert.assertEquals(20.0, ans3.getScore());
        Assert.assertEquals(20.0, ans4.getScore());
        Assert.assertEquals(15.0, ans5.getScore());
        Assert.assertEquals(14.0, ans6.getScore());
        Assert.assertEquals(5.0, ans7.getScore());
        Assert.assertEquals(null, ans8);
        Assert.assertEquals(null, ans9);

        for (int i = 5; i <= 20; i++) {
            instance.delete(i, str);
        }
    }

    @Test
    public void testDeleteRangeByScore() {
        SkipList instance = SkipList.createSkipList();
        String str = "a";
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        System.out.println("删除前===============================");
        instance.showStructure();
        SkipList.ScoreRangeSpec range1 = new SkipList.ScoreRangeSpec(true, 5, true, 20);
        long ans1 = instance.deleteRangeByScore(range1);
        Assert.assertEquals(16, ans1);
        System.out.println("删除后===============================");
        instance.showStructure();


        instance = SkipList.createSkipList();
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        System.out.println("删除前===============================");
        instance.showStructure();
        SkipList.ScoreRangeSpec range2 = new SkipList.ScoreRangeSpec(false, 5, false, 20);
        long ans2 = instance.deleteRangeByScore(range2);
        Assert.assertEquals(14, ans2);
        System.out.println("删除后===============================");
        instance.showStructure();

        instance = SkipList.createSkipList();
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        System.out.println("删除前===============================");
        instance.showStructure();
        SkipList.ScoreRangeSpec range3 = new SkipList.ScoreRangeSpec(true, 10, true, 15);
        long ans3 = instance.deleteRangeByScore(range3);
        Assert.assertEquals(6, ans3);
        System.out.println("删除后===============================");
        instance.showStructure();


        instance = SkipList.createSkipList();
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        System.out.println("删除前===============================");
        instance.showStructure();
        SkipList.ScoreRangeSpec range4 = new SkipList.ScoreRangeSpec(true, 1, true, 35);
        long ans4 = instance.deleteRangeByScore(range4);
        Assert.assertEquals(16, ans4);
        System.out.println("删除后===============================");
        instance.showStructure();
    }

    @Test
    public void testDeleteRangeByRank() {
        SkipList instance = SkipList.createSkipList();
        String str = "a";
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        System.out.println("删除前===============================");
        instance.showStructure();
        long ans1 = instance.deleteRangeByRank(2, 3);
        Assert.assertEquals(2, ans1);
        System.out.println("删除后===============================");
        instance.showStructure();


        instance = SkipList.createSkipList();
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        System.out.println("删除前===============================");
        instance.showStructure();
        long ans2 = instance.deleteRangeByRank(1, 6);
        Assert.assertEquals(6, ans2);
        System.out.println("删除后===============================");
        instance.showStructure();

        instance = SkipList.createSkipList();
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        System.out.println("删除前===============================");
        instance.showStructure();
        long ans3 = instance.deleteRangeByRank(3, 2);
        Assert.assertEquals(0, ans3);
        System.out.println("删除后===============================");
        instance.showStructure();


        instance = SkipList.createSkipList();
        for (int i = 5; i <= 20; i++) {
            instance.insert(i, str);
        }

        System.out.println("删除前===============================");
        instance.showStructure();
        long ans4 = instance.deleteRangeByRank(5, 40);
        Assert.assertEquals(12, ans4);
        System.out.println("删除后===============================");
        instance.showStructure();
    }

}
