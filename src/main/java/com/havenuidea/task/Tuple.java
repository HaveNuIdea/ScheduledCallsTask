package com.havenuidea.task;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Class represents pair of date and call
 */
public class Tuple {

    private Date date;
    private Callable call;

    public Tuple(Date date, Callable call) {
        this.date = date;
        this.call = call;
    }

    public Date getDate() {
        return date;
    }

    public Callable getCall() {
        return call;
    }

}
