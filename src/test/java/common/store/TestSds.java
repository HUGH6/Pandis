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
}
