package com.hzgc.dubbo.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DubboProvider {

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("conf/dubbo-provider.xml");

        context.start();

        System.out.println("Service started...");

        while (true) {
        }

    }
}
