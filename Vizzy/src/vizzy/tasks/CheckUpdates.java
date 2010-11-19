/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.tasks;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import vizzy.listeners.IUpdateCheckListener;
import vizzy.model.Conf;
import vizzy.util.DialogUtils;

/**
 *
 * @author sergeil
 */
public class CheckUpdates extends Thread {

    private static final Logger log = Logger.getLogger(CheckUpdates.class);
    
    private boolean reportIfOk;
    private IUpdateCheckListener listener;

    public CheckUpdates(boolean reportIfOk, IUpdateCheckListener listener) {
        this.reportIfOk = reportIfOk;
        this.listener = listener;
    }

    @Override
    public void run() {
        boolean downloaded = false;
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

            int i = r.indexOf(Conf.WEBSITE_UPDATE_PHRASE);
            int i2 = r.indexOf(";", i);
            if (i > -1) {
                String newVer = r.substring(i + Conf.WEBSITE_UPDATE_PHRASE.length(), i2);
                double newVerd = Double.parseDouble(newVer);
                double verd = Double.parseDouble(Conf.VERSION);
                if (newVerd > verd) {

                    i = r.indexOf(Conf.WEBSITE_FEATURES_PHRASE);
                    i2 = r.indexOf(";", i);
                    String newFeatures = r.substring(i + Conf.WEBSITE_FEATURES_PHRASE.length(), i2);
                    newFeatures = newFeatures.replaceAll("\\|", "\n");

                    listener.offerUpdate();
                    
                    Object[] options = {"Yes",
                        "No",};

                    int reply = JOptionPane.showOptionDialog(null, "New version is availible (" + newVer + ").\n" +
                            "Would you like Vizzy to download it?\n"
                            + "Note: if you do not want to receive update\n"
                            + "notifications anymore, you can turn\n" +
                            "them off in Options menu.\n\n" +
                            "New features:\n" +
                            newFeatures,
                            "New Version",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (reply == JOptionPane.YES_OPTION) {
                        downloaded = true;
                        downloadNewVersion(newVer);
                    }
                } else {
                    if (reportIfOk) {
                        JOptionPane.showMessageDialog(null, "You have the latest version!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("run() error", ex);
        } finally {
            listener.updateFinished(downloaded);
            listener = null;
        }
        
    }

    private void downloadNewVersion(String newVer) {
        String filename = String.format("Vizzy-%s-%s.zip", Conf.OSShortName, newVer);
//        String filename = String.format("Vizzy_v%s.zip", newVer);
        String fileUrl = "http://flash-tracer.googlecode.com/files/" + filename;
        try {

            DialogUtils.showDialog("Downloading " + filename + "...");

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

            File saveFile = new File(filename);
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(saveFile);
            DialogUtils.setVisible(false);
            while (true) {
                int choice = fileChooser.showSaveDialog(null);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File chosen = fileChooser.getSelectedFile();
                    saveFile = chosen;
                    break;
                } else {
                    break;
                }
            }

            FileUtils.copyFile(tmpFile, saveFile);
            
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(saveFile);
            }


            DialogUtils.closeDialog();
        } catch (Exception ex) {
            DialogUtils.closeDialog();

            log.warn("downloadNewVersion() failed to download uptdate", ex);

            JOptionPane.showMessageDialog(null, "Failed to download update. Press OK to go\n"
                    + "to the web-site and to download\n"
                    + "new version manually!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            try {
                if (Desktop.isDesktopSupported())
                    Desktop.getDesktop().browse(new URI(Conf.URL_PROJECT_DOWNLOAD));
            } catch (Exception ex1) {
//                log.warn("downloadNewVersion() desktop not supported", ex);
            }

        } 
    }
}
