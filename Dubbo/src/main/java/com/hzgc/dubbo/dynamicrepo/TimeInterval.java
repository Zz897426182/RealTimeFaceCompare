package com.hzgc.dubbo.dynamicrepo;

/**
 * 一天内的时间区间
 */
public class TimeInterval {
    /**
     * 开始时间，以一天内的分钟数计算
     */
    private int start;
    /**
     * 结束时间，以一天内的分钟数计算
     */
    private int end;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "TimeInterval{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
