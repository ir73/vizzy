/*
 * LoadFileTask.java
 *
 * Created on 21 March 2007, 19:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package vizzy.tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import vizzy.listeners.ILogFileListener;
import vizzy.model.SettingsModel;

/**
 *
 * @author Admin
 */
public class LoadFileTask extends TimerTask {

    private static final Logger log = Logger.getLogger(LoadFileTask.class);

    private SettingsModel settings;
    private ILogFileListener listener;

    public LoadFileTask(SettingsModel settings, ILogFileListener listener) {
        this.settings = settings;
        this.listener = listener;
    }

    @Override
    public void run() {

        ByteArrayOutputStream bo = null;
        RandomAccessFile raf = null;
        try {
            File inputFile = new File(settings.getCurrentLogFile());
            raf = new RandomAccessFile(inputFile, "r");
            bo = new ByteArrayOutputStream();

            int len = (int) raf.length();
            if (settings.isMaxNumLinesEnabled() && len > settings.getMaxNumLines()) {
                int v = (int) (len - settings.getMaxNumLines());
                raf.seek(v);
            }

            byte[] b = new byte[4096];
            int count = 0;
            while ((count = raf.read(b)) != -1) {
                bo.write(b, 0, count);
            }

            String s = null;
            if (settings.isUTF()) {
                s = new String(bo.toByteArray(), "UTF-8");
            } else {
                s = new String(bo.toByteArray());
            }

            listener.onLogFileRead(s);

        } catch (OutOfMemoryError ex) {
            listener.onOutOfMemory();
        } catch (FileNotFoundException ex) {
            listener.onLogFileRead("");
        } catch (Exception ex) {
            log.warn("run() ", ex);
        } finally {
            try {
                bo.close();
            } catch (Exception ex) {
            }
            try {
                raf.close();
            } catch (Exception ex) {
            }
        }

    }
}
