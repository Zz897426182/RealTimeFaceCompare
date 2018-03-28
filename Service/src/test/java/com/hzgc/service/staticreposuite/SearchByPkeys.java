package com.hzgc.service.staticreposuite;

import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017-8-22.
 */
public class SearchByPkeys {
    public static void main(String[] args) {
//        ObjectInfoInnerHandlerImpl objectInfoInnerHandler = new ObjectInfoInnerHandlerImpl();
//        List<String> a  =  objectInfoInnerHandler.searchByPkeys();
//        for (String str: a){
//            System.out.println(str);
//        }
//        System.out.println(a.size());
    }

    @Test
    public void testDemoRegexPattern(){
        String pattern = "[-_A-Za-z0-9\\u4e00-\\u9fa5]{0,30}";
        System.out.println(Pattern.matches(pattern, "nima-李_0A"));
    }
}
