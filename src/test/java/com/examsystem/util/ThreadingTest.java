package com.examsystem.util;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Threading utilities tests per context/TESTING_GUIDE.md Phase 8.
 */
public class ThreadingTest {

    @Test
    public void testExamTimerThreadCountsDown() throws InterruptedException {
        AtomicInteger lastTick = new AtomicInteger(-1);
        CountDownLatch expired = new CountDownLatch(1);

        ExamTimerThread timer = new ExamTimerThread(2, lastTick::set, expired::countDown);
        timer.start();

        assertTrue(expired.await(5, TimeUnit.SECONDS));
        assertEquals(0, lastTick.get());
        timer.shutdown();
    }

    @Test
    public void testThreadPoolManagerSubmitsBackgroundTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ThreadPoolManager.getInstance().submitBackground(latch::countDown);
        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }
}
