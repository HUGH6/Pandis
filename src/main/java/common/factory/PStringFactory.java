package common.factory;

import common.store.PString;

public class PStringFactory {
    public static PString createPString(int len, int free, byte[] init) {
        if(len < 0) {
            throw new NegativeArraySizeException();
        } else if (len > 2147483647){
            throw new OutOfMemoryError("len is " + len + ", should be less than " + 2147483647);
        } else {
            if(init != null) {
                byte[] ret = new byte[len];
                System.arraycopy(init, 0, ret, 0, len);
                return new PString(len, free, ret);
            } else {
                return new PString(len, free, null);
            }
        }
    }
}