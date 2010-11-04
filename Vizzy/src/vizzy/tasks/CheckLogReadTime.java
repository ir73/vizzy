/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.tasks;

import vizzy.listeners.ILogFileListener;

/**
 *
 * @author sergeil
 */
public class CheckLogReadTime extends Thread {
    private boolean keepRunning = true;
    private ILogFileListener listener;
    private final long sleepTime;

    public CheckLogReadTime(ILogFileListener listener, long sleepTime) {
        super();
        this.listener = listener;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            dispose();
            return;
        }
        
        if (!keepRunning) {
            dispose();
            return;
        }

        listener.onOutOfMemory();
    }

    private void dispose() {
        listener = null;
    }

    public void stopRunning() {
        keepRunning = false;
    }

}
