package event;

import common.store.PString;
import org.junit.Test;

public class TestPString {
    @Test
    public void test() {
        byte[] b = new byte[4];
        b[0] = 'a';
        b[1] = 20;
        System.out.println(b[0]);
        System.out.println(b[1]);
    }

    @Test
    public void test2() {
        byte[] b = new byte[5];
        b[0] = 0;
        b[1] = 1;
        b[2] = 2;
        b[3] = 3;
        b[4] = 4;

        byte[] b2 = new byte[3];
        b2[0] = 10;
        b2[1] = 20;
        b2[2] = 30;

        System.arraycopy(b2, 0, b, 0, 3);
        for(int i = 0; i < 5; i++) {
            System.out.println(b[i]);
        }
    }

    @Test
    public void test3() {
        System.out.println("=======1========");
        byte[] b = new byte[5];
        b[0] = 0;
        b[1] = 1;
        b[2] = 2;
        b[3] = 3;
        b[4] = 4;
        PString p = new PString();
        p = p.pStringNewLen(b, 10);
        p.pStringPrintBuf();
        System.out.println(p.getLen());
        System.out.println(p.getFree());

        System.out.println("========2===========");
        p.pStringEmpty();
        p.pStringPrintBuf();
        p.pStringPrintLen();
        p.pStringPrintFree();

        System.out.println("========3===========");
        char[] c = new char[3];
        c[0] = 'a';
        c[1] = 'b';
        c[2] = '3';
        p.pStringNew(c);
        p.pStringPrintAll();

        System.out.println("========4===========");
        byte[] b3 = new byte[5];
        b3[0] = 10;
        b3[1] = 11;
        b3[2] = 12;
        b3[3] = 13;
        b3[4] = 14;
        PString p2 = new PString(5, 10, b3);
        p.pStringUp(p2);
        p.pStringPrintAll();

        System.out.println("========5===========");
        p.pStringFree();
        p.pStringPrintAll();
        System.out.println("========");
        p.pStringUp(p2);
        p.pStringMakeRoomFor(10);
        p.pStringPrintAll();

        System.out.println("========6===========");
        p.pStringRemoveFreeSpace();
        p.pStringPrintAll();

        System.out.println("========7===========");
        p.pStringResize(3, 5);
        p.pStringPrintAll();

        System.out.println("========8===========");
        System.out.println(p.pStringSize());

        System.out.println("========9===========");
        p.pStringPrintAll();
        System.out.println("=====9.1===");
        p.pStringIncLen(10);
        p.pStringPrintAll();
        System.out.println("=====9.2===");
        p.pStringIncLen(5);
        p.pStringPrintAll();

        System.out.println("========10===========");
        p.pStringGrowZero(20);
        p.pStringPrintAll();

        System.out.println("========11===========");
        byte[] b4 = new byte[5];
        b4[0] = 0;
        b4[1] = 1;
        b4[2] = 2;
        b4[3] = 3;
        b4[4] = 4;
        PString p3 = new PString();
        p3 = p2.pStringNewLen(b4, 10);
        p.pStringCatLen(p3, 10);
        p.pStringPrintAll();
        System.out.println("======11.0======");
        p.pStringCatPString(p3);
        p.pStringPrintAll();
        System.out.println("======11.1======");
        char[] c2 = new char[3];
        c2[0] = 'a';
        c2[1] = 'b';
        c2[2] = 'c';
        PString p4 = p.pStringCat(c2);
        p4.pStringPrintAll();

        System.out.println("=========12============");
        byte[] b5 = new byte[5];
        b5[0] = 'a';
        b5[1] = 'b';
        b5[2] = 'c';
        b5[3] = 'd';
        b5[4] = 'e';
        PString p5 = new PString();
        p5 = p5.pStringNewLen(b5, 10);
        PString p6 = p.pStringCpyLen(p5, 5);
        p.pStringPrintAll();

        System.out.println("=========13============");
        p5.pStringToUpper();
        p5.pStringPrintAll();
        p5.pStringToLower();
        p5.pStringPrintAll();

        System.out.println("==========14==========");
        byte[] b6 = new byte[5];
        b6[0] = 'a';
        b6[1] = 'B';
        b6[2] = 'c';
        b6[3] = 'd';
        b6[4] = 'e';
        p6 = p6.pStringNewLen(b6, 10);

        byte[] b7 = new byte[5];
        b7[0] = 'a';
        b7[1] = 'b';
        b7[2] = 'C';
        b7[3] = 'd';
        b7[4] = 'e';
        PString p7 = new PString();
        p7 = p7.pStringNewLen(b7, 10);

        int ans = p6.pStringCmp(p7);
        System.out.println(ans);
    }

}