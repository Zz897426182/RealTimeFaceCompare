import java.util.concurrent.CountDownLatch;

/**
 * 手动创建线程，便于统计执行的总时间
 */
public class StopLatchedThread extends Thread {
    private final CountDownLatch stopLatch;

    private StopLatchedThread(CountDownLatch stopLatch) {
        this.stopLatch = stopLatch;
    }

    public void run() {
        try {
            UpDataToFtp.upDataTest("e:/test1", 1, "test");
        } finally {
            stopLatch.countDown();
        }
    }

    private static void performParallelTask() throws InterruptedException {
        //机器可用核数
        int cores = Runtime.getRuntime().availableProcessors();
        CountDownLatch cdl = new CountDownLatch(cores);
        for (int i = 0; i < cores; i++) {
            Thread t = new StopLatchedThread(cdl);
            t.start();
        }
        cdl.await();
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        performParallelTask();
        System.out.println("执行总时间：" + (System.currentTimeMillis() - start) / 1000);
    }
}

