package com.havenuidea.task;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExecutionSchedulerTest {

    static int i = 0;

    private Callable call = new Callable() {
        @Override
        public Object call() throws Exception {
            System.out.println("call " + i++);
            return null;
        }
    };

    @Test
    public void testAdd() throws InterruptedException {
        i=0;
        ExecutionScheduler es = new ExecutionScheduler();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MILLISECOND, 3000);
        Date d1 = new Date(c.getTimeInMillis());
        es.add(d1, call);
        Date d = new Date();
        es.add(d, call);
        try {
            es.add(new Date(), null);
        } catch (NullPointerException ignore){
        }
        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.MILLISECOND, -5000);
        Date d2 = new Date(c2.getTimeInMillis());
        es.add(d2, call);
        es.add(d2, call);        
        Thread.sleep(10000L);
        es.close();
    }

    @Test(expected = NullPointerException.class)    
    public void testAddNull() throws InterruptedException {
        System.out.println("testAddNull");
        ExecutionScheduler es = new ExecutionScheduler();
        es.add(new Date(), null);
        es.close();
    }
    
    @Test
    public void testAddAndClose() throws InterruptedException {
        i=0;
        System.out.println("testAddAndClose");
        ExecutionScheduler es = new ExecutionScheduler();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MILLISECOND, 300);
        Date d1 = new Date(c.getTimeInMillis());
        es.add(d1, call);
        Date d = new Date();
        es.add(d, call);
        Thread.sleep(150L);
        es.close();
        String err = "";
        try {
            es.add(new Date(), call);
        } catch (IllegalStateException e){
            err = e.getMessage();
        }
        assertEquals("Exception didn't occur", "Execution stopped. Can't add new tasks to run.", err);
        Thread.sleep(1000L);
    }
}