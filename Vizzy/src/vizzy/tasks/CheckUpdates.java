/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.tasks;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import vizzy.listeners.IUpdateCheckListener;
import vizzy.model.Conf;

/**
 *
 * @author sergeil
 */
public class CheckUpdates extends Thread {

    private static final Logger log = Logger.getLogger(CheckUpdates.class);
    
    private boolean isSilent;
    private IUpdateCheckListener listener;
    private boolean downloaded;

    public CheckUpdates(IUpdateCheckListener listener) {
        this.isSilent = true;
        this.listener = listener;
    }

    @Override
    public void run() {
        downloaded = false;

        try {


            String content = getPageContent();
            if (content == null) {
                cleanUp();
                return;
            }

            String nw = getNewVersion(content);
            if (nw == null) {
                cleanUp();
                return;
            }

            double newVerd = Double.parseDouble(nw);
            double verd = Double.parseDouble(Conf.VERSION);

            if (newVerd > verd) {

                File tmpFile = null;
                try {
                    tmpFile = downloadNewVersion(nw);
                } catch (Exception ex) {
                    log.warn("run() failed to run 'downloadNewVersion'", ex);
                    showFailedToDownloadAutomatically();
                }

                if (tmpFile == null) {
                    cleanUp();
                    return;
                }

                String message = null;
                String newFeatures = null;
                try {
                    int i = content.indexOf(Conf.WEBSITE_FEATURES_PHRASE);
                    int i2 = content.indexOf(";", i);
                    newFeatures = content.substring(i + Conf.WEBSITE_FEATURES_PHRASE.length(), i2);

                    if (newFeatures == null) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(tmpFile);
                                downloaded = true;
                            } catch (IOException ex) {
                            }
                        }
                        cleanUp();
                        return;
                    }
                } catch (Exception e) {
                    log.warn("run() update error 2", e);
                }

                if (newFeatures == null) {
                    message = "New version has been downloaded (" + nw + ")!\n"
                        + "Click OK to install new version.";
                } else {

                    newFeatures = newFeatures.replaceAll("\\|", "\n");

                    message = "New version has been downloaded (" + nw + ")!\n"
                        + "Click OK to install new version.\n\n"
                        + "New features:\n"
                        + newFeatures;
                }

                Object[] options = {"OK",
                    "Cancel",};

                int reply = JOptionPane.showOptionDialog(null, message,
                        "Update",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (reply == JOptionPane.OK_OPTION) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(tmpFile);
                            downloaded = true;
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("run() update error", e);
        }
        cleanUp();
    }

    private File downloadNewVersion(String newVer) throws Exception {
        String filename = String.format("Vizzy-%s-%s.zip", Conf.OSShortName, newVer);
        String fileUrl = "http://flash-tracer.googlecode.com/files/" + filename;
        try {

            byte[] bytes = new byte[1024];
            URL u = new URL(fileUrl);
            URLConnection openConnection = u.openConnection();
            InputStream isr = openConnection.getInputStream();

            File tmpFile = File.createTempFile("Vizzy-" + newVer, ".zip");
            FileOutputStream fos = new FileOutputStream(tmpFile);
            int len = 0;
            while ((len = isr.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }

            fos.close();
            isr.close();

            return tmpFile;
        } catch (Exception ex) {
            throw ex;
        }
    }

    private String getPageContent() {
        try {
            URL u = new URL(Conf.URL_PROJECT_HOME);
            URLConnection openConnection = u.openConnection();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    openConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine)
                        .append("\n");
            }
            in.close();

            String r = response.toString();
            return r;
        } catch (Exception ex) {
            log.warn("getNewVersion() error", ex);
        }
        return null;
    }

    private String getNewVersion(String content) {
        int i = content.indexOf(Conf.WEBSITE_UPDATE_PHRASE);
        int i2 = content.indexOf(";", i);
        if (i > -1) {
            String newVer = content.substring(i + Conf.WEBSITE_UPDATE_PHRASE.length(), i2);
            return newVer;
        }
        return null;
    }

    private void showFailedToDownloadAutomatically() {
        JOptionPane.showMessageDialog(null, "Failed to download update. Please visit product's home page\n"
                + "and download new version manually.",
                "Error",
                JOptionPane.ERROR_MESSAGE);

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(Conf.URL_PROJECT_DOWNLOAD));
            }
        } catch (Exception ex1) {
            //                log.warn("downloadNewVersion() desktop not supported", ex);
        }
    }

    private void cleanUp() {
        listener.updateFinished(downloaded);
        listener = null;
    }
}
