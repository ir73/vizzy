/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.tasks;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import vizzy.listeners.IUpdateCheckListener;
import vizzy.model.Conf;

/**
 *
 * @author sergeil
 */
public class CheckUpdates extends Thread {

    private static final Logger log = Logger.getLogger(CheckUpdates.class);
    
    private IUpdateCheckListener listener;
    private File updateDir;

    public CheckUpdates(IUpdateCheckListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {

            String content = getPageContent();
            if (content == null) {
                cleanUp();
                return;
            }

            String newVersion = getNewVersion(content);
            if (newVersion == null) {
                cleanUp();
                return;
            }

            double dNewVersion = Double.parseDouble(newVersion);
            double dCurrentVersion = Double.parseDouble(Conf.VERSION);

            if (dNewVersion <= dCurrentVersion) {
                cleanUp();
                return;
            }

            updateDir = new File(Conf.vizzyRootDir, Conf.UPDATE_FOLDER);
            if (updateDir.exists() && updateDir.list().length > 0) {
                showPendingUpdateMessage();
                return;
            }
            updateDir.mkdirs();

            File downloadedZip = null;
            try {
                downloadedZip = downloadNewVersion(newVersion);
            } catch (Exception ex) {
                log.warn("run() failed to run 'downloadNewVersion'", ex);
                downloadedZip = null;
            }

            if (downloadedZip == null) {
                showFailedToDownloadAutomatically();
                return;
            }

            String message = null;
            String newFeatures = null;
            try {
                newFeatures = getNewFeatures(content);
            } catch (Exception ex) {
                log.warn("run() failed to get new features", ex);
            }

            if (newFeatures == null) {
                message = "New version has been downloaded (" + newVersion + ")!\n"
                        + "Click OK to install new version.";
            } else {
                newFeatures = newFeatures.replaceAll("\\|", "\n");
                message = "New version has been downloaded (" + newVersion + ")!\n"
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
                        Desktop.getDesktop().open(downloadedZip);
                        exit();
                    } catch (IOException ex) {
                        log.warn("error opening downloaded update " + downloadedZip.getAbsolutePath());
                        JOptionPane.showMessageDialog(null, "Cannot execute downloaded update. Please open it manually:\n"
                                + downloadedZip.getAbsolutePath(), "Update", JOptionPane.ERROR_MESSAGE);
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
        File f = new File(updateDir.getAbsolutePath(), filename);
        try {
            FileUtils.copyURLToFile(new URL(fileUrl), f);
        } catch (Exception e) {
            log.warn("downloadNewVersion() cannot create temp file :"  + f.getAbsolutePath());
            f = File.createTempFile(filename, ".zip");
            FileUtils.copyURLToFile(new URL(fileUrl), f);
        }
        return f;
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
        Object[] options = {"OK",
            "Cancel",};
        int reply = JOptionPane.showOptionDialog(null, "Failed to download update. Click OK to visit product's home page\n"
                + "and download new version manually.",
                "Update",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (reply == JOptionPane.OK_OPTION) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(Conf.URL_PROJECT_DOWNLOAD));
                }
                exit();
            } catch (Exception ex1) {
                //                log.warn("showFailedToDownloadAutomatically() desktop not supported", ex);
            }
        } else {
            cleanUp();
        }
    }

    private void exit() {
        listener.exit();
        cleanUp();
    }

    private void cleanUp() {
        listener = null;
    }

    private String getNewFeatures(String content) throws Exception {
        int i = content.indexOf(Conf.WEBSITE_FEATURES_PHRASE);
        int i2 = content.indexOf(";", i);
        return content.substring(i + Conf.WEBSITE_FEATURES_PHRASE.length(), i2);
    }

    private void showPendingUpdateMessage() {
        Object[] options = {"OK",
            "Cancel",};
        int reply = JOptionPane.showOptionDialog(null, "You have a pending update located in\n"
                + updateDir.getAbsolutePath() + "\n"
                + "Would you like to install it now?",
                "Update",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (reply == JOptionPane.OK_OPTION) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(updateDir);
                }
                exit();
            } catch (Exception ex1) {
                //                log.warn("showPendingUpdateMessage() desktop not supported", ex);
            }
        } else {
            cleanUp();
        }
    }
}
