/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.tasks;

import java.util.TimerTask;
import vizzy.controller.VizzyController;

/**
 *
 * @author sergeil
 */
public class HideCodePopupTimerTask extends TimerTask {
    private VizzyController aThis;

    public HideCodePopupTimerTask(VizzyController aThis) {
        this.aThis = aThis;
    }

    @Override
    public void run() {
        aThis.onHideCodePopup();
        aThis = null;
    }

}
