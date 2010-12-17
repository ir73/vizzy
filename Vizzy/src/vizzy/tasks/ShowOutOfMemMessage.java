/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.tasks;

import vizzy.listeners.OutOfMemoryDisplayedListener;

/**
 *
 * @author sergeil
 */
public class ShowOutOfMemMessage extends Thread {
    
    private OutOfMemoryDisplayedListener outOfMemoryDisplayedListener;

    public ShowOutOfMemMessage(OutOfMemoryDisplayedListener outOfMemoryDisplayedListener) {
        this.outOfMemoryDisplayedListener = outOfMemoryDisplayedListener;
        
    }

    @Override
    public void run() {
        outOfMemoryDisplayedListener.messageDisplayed();
        outOfMemoryDisplayedListener = null;
    }

    
}
