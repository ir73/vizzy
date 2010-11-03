/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.tasks;

import java.io.File;
import vizzy.model.Conf;
import vizzy.model.FlashPlayerFiles;

/**
 *
 * @author sergeil
 */
public class FlashPlayerFilesLocator {

    public static FlashPlayerFiles findFilesPaths() {

        FlashPlayerFiles fpf = new FlashPlayerFiles();
        
        String logPath = null;
        String osName = Conf.OSName;
        String home = Conf.userHome;

        if (osName == null || home == null) {
            return fpf;
        }

        if (osName.indexOf(Conf.OS_WINDOWS_VISTA) > -1) {
            logPath = home + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "Macromedia" + File.separator + "Flash Player" + File.separator + "Logs" + File.separator + "flashlog.txt";
        } else if (osName.indexOf(Conf.OS_WINDOWS) > -1) {
            logPath = home + File.separator + "Application Data" + File.separator + "Macromedia" + File.separator + "Flash Player" + File.separator + "Logs" + File.separator + "flashlog.txt";
        } else if (osName.indexOf(Conf.OS_MAC_OS_X) > -1) {
            logPath = home + "/Library/Preferences/Macromedia/Flash Player/Logs/flashlog.txt";
        } else if (osName.indexOf(Conf.OS_LINUX) > -1) {
            logPath = home + "/.macromedia/Flash_Player/Logs/flashlog.txt";
        }

        fpf.setLogPath(logPath);
        
        return fpf;
    }
}
