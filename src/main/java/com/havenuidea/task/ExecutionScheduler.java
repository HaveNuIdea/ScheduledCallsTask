package com.havenuidea.task;

import java.util.Date;
import java.util.concurrent.*;
import org.apache.log4j.*;

/**
 * Take given Date and Callable and execute Callable on given date
 */
public class ExecutionScheduler {

    private static final Logger logger = Logger.getLogger(ExecutionScheduler.class.getName());

    //Execution state
    private volatile boolean running = true;

    //Thread pool to execute callables
    private final ExecutorService service = Executors.newCachedThreadPool();

    //Thread pool to check for task that are ready to run
    private final ScheduledExecutorService starter = Executors.newSingleThreadScheduledExecutor();

    //Queue to store tasks in order
    private final PriorityBlockingQueue<Tuple> queue = new PriorityBlockingQueue<>(100, (t1, t2) -> {
            Date d1 = t1.getDate();
            Date d2 = t2.getDate();
            if (d1.after(d2)) {
                return 1;
            } else if (d1.equals(d2)) {
                return 0;
            }
            return -1;
        }
    );

    /**
     * Start execution on schedule
     */
    public ExecutionScheduler() {
        starter.scheduleWithFixedDelay(() -> {
            Tuple t = queue.peek();
            while (t != null && t.getDate().before(new Date()) && running) {
                t = queue.poll();
                Callable c = t.getCall();
                service.submit(c);
                t = queue.peek();
            }
        }, 0L, 50L, TimeUnit.MILLISECONDS);
    }

    /**
     * Add call to execute it at given date
     *
     * @param date
     * @param call
     */
    public void add(Date date, Callable call) {
        if (!running) {
            throw new IllegalStateException("Execution stopped. Can't add new tasks to run.");
        }
        if (call != null) {
            queue.add(new Tuple(date, call));
        } else {
            throw new NullPointerException("Callable can't be null.");
        }
    }

    /**
     * Stop schedulers
     */
    public void close() {
        running = false;
        starter.shutdown();
        try {
            if (!starter.awaitTermination(1, TimeUnit.MINUTES)) {
                starter.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("Starter termination was interrupted", e);
        }

        service.shutdown();
        try {
            if (!service.awaitTermination(1, TimeUnit.MINUTES)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("service thread pool termination was interrupted", e);
        }
    }
}