package com.hzgc.ftpserver.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 缓冲队列(刘善彬 to 内)
 */
public class BufferQueue {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    private static final BufferQueue bufferQueue = new BufferQueue();

    private BufferQueue() {
    }

    public static BufferQueue getInstance() {
        return bufferQueue;
    }

    public BlockingQueue<String> getQueue() {
        return queue;
    }

}
