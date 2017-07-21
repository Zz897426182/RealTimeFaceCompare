package com.hzgc.hbase.staticrepo;

import java.util.Random;

public class StaticRepoUtil {
    public static String getIdCardByRandom(){
        char[] alpha = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                                    'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
                                    'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a',
                                    'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                                    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
                                    't', 'u', 'v', 'w', 'x', 'y', 'z'};
        StringBuffer idcard = new StringBuffer("");
        Random random = new Random();
        for (int i = 0; i < 18; i++){
            idcard.append(alpha[random.nextInt(52)]);
        }
        return new String(idcard);
    }
    public static String getPkeyByRandom(){
        char[] alpha = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                                    'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
                                    'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a',
                                    'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                                    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
                                    't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1',
                                    '2', '3', '4', '5', '6', '7', '8', '9'};
        StringBuffer pkey = new StringBuffer("");
        Random random = new Random();
        for (int i = 0; i < 6; i++){
            pkey.append(alpha[random.nextInt(62)]);
        }
        return new String(pkey);
    }
}
