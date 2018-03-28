package com.hzgc.ftpserver.util;

import com.codahale.metrics.*;

import java.util.Arrays;
import java.util.List;

public class CounterTest {
    private static MetricRegistry metric = new MetricRegistry();
    /*
    记录执行次数
     */
    private final static Counter counter = metric.counter(MetricRegistry.name(CounterTest.class, "counter"));
    /*
     获取某个值   获取当前JVM可用内存
     */
    private final static Gauge<Long> gauge = metric.register("gauge", () -> Runtime.getRuntime().freeMemory());
    /*
    Meter用来计算事件的速率
     */
    private final static Meter meter = metric.meter("meter");
    /*
     用来测量一段代码被调用的速率和用时。等于Meter+Hitogram，既算TPS，也算执行时间。
     */
    private final static Timer timer = metric.timer(MetricRegistry.name(CounterTest.class, "timer"));
    /*
    可以为数据流提供统计数据。 除了最大值，最小值，平均值外，它还可以测量 中值(median)，百分比比如XX%这样的Quantile数据
     */
    private final static Histogram histogram = metric.histogram(MetricRegistry.name(CounterTest.class, "histogram"));

    /**
     * 使用metrics监测的方法
     */
    private static void test() {
        counter.inc();
        meter.mark();
        timer.time();
        List<Integer> list = Arrays.asList(60, 75, 80, 62, 90, 42, 33, 95, 61, 73);
        for (int i : list) {
            histogram.update(i);
        }
    }

    public static void main(String[] args) {
        test();
        Counter counter1 = counter;
        System.out.println("count          :" + counter.getCount());
        Gauge<Long> gauge1 = gauge;
        System.out.println("freeMemory     :" + gauge.getValue());
        Metric metric1 = meter;
        System.out.println("count          :" + meter.getCount());
        System.out.println("mean rate      :" + meter.getMeanRate());
        System.out.println("1-minute rate  :" + meter.getOneMinuteRate());
        System.out.println("5-minute rate  :" + meter.getFiveMinuteRate());
        System.out.println("15-minute rate :" + meter.getFifteenMinuteRate());
        Timer.Context time1 = timer.time();
        System.out.println("timer          :" + timer.time().toString());
        Histogram histogram1 = histogram;
        System.out.println("histogram      :" + histogram.toString());
    }
}
