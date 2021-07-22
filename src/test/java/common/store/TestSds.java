package common.store;

import org.junit.Assert;
import org.junit.Test;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-19
 */
public class TestSds {
    @Test
    public void testCreateSds() {
        byte [] str = {'h','i','j'};
        Sds s1 = Sds.createSds(str);
        Assert.assertEquals(3, s1.getLen());
        Assert.assertEquals(0, s1.getFree());
        Assert.assertEquals("hij", s1.toString());

        Sds s2 = Sds.createSds(5, str);
        Assert.assertEquals(3, s2.getLen());
        Assert.assertEquals(2, s2.getFree());
        Assert.assertEquals("hij", s2.toString());

    }

    @Test
    public void testIndexOf() {
        byte [] str = {'h','i','j', '\n'};
        Sds s1 = Sds.createSds(str);
        Assert.assertEquals(1, s1.indexOf('i'));
        Assert.assertEquals(2, s1.indexOf('j'));
        Assert.assertEquals(-1, s1.indexOf('o'));
        Assert.assertEquals(3, s1.indexOf('\n'));
    }

    @Test
    public void testIsWhiteSpace(){
//        System.out.println(Character.isWhitespace('c'));
//        System.out.println(Character.isWhitespace(' '));
//        System.out.println(Character.isWhitespace('\t'));
//        System.out.println(Character.isWhitespace('\n'));
////        System.out.println(Character.isWhitespace('\v'));
//        System.out.println(Character.isWhitespace('\f'));
//

        byte [] str = {' ','4','\r','\n','\t','\f'};
        Sds s1 = Sds.createSds(str);
        int i = 0;
//        while (i++ <= 3){
//            System.out.println(Character.isWhitespace(str[i]));
//        }


        System.out.println(Character.isDigit((char)str[i]));
        System.out.println(Character.isDigit((char)str[i+1]));



    }
}
