package org.cyanotic.cx10.common;

/**
 * Created by cyanotic on 01/12/2016.
 */
public class KillableThread extends Thread {

    private boolean isKilled;

    public KillableThread() {
        super();
    }

    public KillableThread(Runnable target) {
        super(target);
        this.isKilled = false;
    }

    public void kill() {
        isKilled = true;
    }

    public boolean isKilled() {
        return isKilled;
    }

    @Override
    public synchronized void start() {
        super.start();
        isKilled = false;
    }
}
