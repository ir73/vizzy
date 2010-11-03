/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.tasks;

import java.awt.Point;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import vizzy.controller.VizzyController;

/**
 *
 * @author sergeilne
 */
public class ShowCodePopupTimerTask extends TimerTask {
    private static final Logger log = Logger.getLogger(ShowCodePopupTimerTask.class);

    private VizzyController aThis;
    private Point pt;

    public ShowCodePopupTimerTask(VizzyController aThis, Point pt) {
        this.aThis = aThis;
        this.pt = pt;
    }

    @Override
    public void run() {
        try {
            aThis.onShowCodePopup(pt);
        } catch (Exception e) {
//            log.warn("run() ", e);
        }
        aThis = null;
        pt = null;
    }

}
