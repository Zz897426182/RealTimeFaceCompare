package com.hzgc.service.device;

public class Test {
    public static void main(String[] args) {
//        List<String> ipc = new ArrayList<>();
//        ipc.add("17130NCY0HZ0002");
//
//        List<WarnRule> rules = new ArrayList<>();
//        WarnRule warnRule1 = new WarnRule();
//        warnRule1.setCode(0);
//        warnRule1.setDayThreshold(3);
//        warnRule1.setThreshold(70);
//        warnRule1.setObjectType("223458");
//        rules.add(warnRule1);
//        WarnRule warnRule2 = new WarnRule();
//        warnRule2.setCode(0);
//        warnRule2.setDayThreshold(70);
//        warnRule2.setThreshold(70);
//        warnRule2.setObjectType("123456");
//        rules.add(warnRule2);
//        WarnRule warnRule3 = new WarnRule();
//        warnRule3.setCode(0);
//        warnRule3.setDayThreshold(4);
//        warnRule3.setThreshold(70);
//        warnRule3.setObjectType("123457");
//        rules.add(warnRule3);
//        WarnRuleServiceImpl warn = new WarnRuleServiceImpl();
//        warn.configRules(ipc, rules);
        DeviceServiceImpl deviceService = new DeviceServiceImpl();
        deviceService.bindDevice("plat2", "17130NCY0HZ0004-g", ".....");
    }
}
