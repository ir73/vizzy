/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.tasks;

import java.awt.Desktop;
import java.io.File;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import vizzy.model.Conf;

/**
 *
 * @author sergeil
 */
public class DebugPlayerDetector {

    private static final Logger log = Logger.getLogger(DebugPlayerDetector.class);

    public DebugPlayerDetector() {
    }

    public void offerDetection() {
        Object[] options = {"Yes",
            "No",};
        int reply = JOptionPane.showOptionDialog(null, "Debug Flash Player detection has not been\n"
                + "performed yet. Would you like to perform it now?",
                "Debug Flash Player Detection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (reply == JOptionPane.YES_OPTION) {
            detect();
        }
    }

    public void detect() {
        try {
            File f = new File(Conf.vizzyRootDir, "fp-detect/fp-detect.html");
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
            }
        } catch (Exception ex) {
            log.warn("cannot open browser", ex);
        }
    }
}
