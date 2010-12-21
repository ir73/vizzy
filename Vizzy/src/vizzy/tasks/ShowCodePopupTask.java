/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.tasks;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import vizzy.forms.panels.CodeForm;
import vizzy.model.SourceAndLine;

/**
 *
 * @author sergeil
 */
public class ShowCodePopupTask {

    private static final Logger log = Logger.getLogger(ShowCodePopupTask.class);

    private JScrollPane owner;
    private CodeForm codeForm;
    private Popup popup;
    private Point codeFormlocationOnScreen;
    private SourceAndLine source;
    private final Object lock = new Object();
    private JTextArea textArea;

    public ShowCodePopupTask() {
        
    }

    public void hide() {
        synchronized (lock) {
            source = null;
            codeFormlocationOnScreen = null;
            if (codeForm != null) {
                codeForm = null;
            }

            if (popup != null) {
                popup.hide();
                popup = null;
            }
        }
    }

    public boolean isVisible() {
        synchronized (lock) {
            return popup != null;
        }
    }

    public boolean isMouseAtCodePopup(Point pt) {
        synchronized (lock) {
            if (codeForm == null) {
                return false;
            }
            if ((pt.getX() > codeFormlocationOnScreen.getX() + codeForm.getWidth())
                    || (pt.getX() < codeFormlocationOnScreen.getX())
                    || (pt.getY() > codeFormlocationOnScreen.getY() + codeForm.getHeight())
                    || (pt.getY() < codeFormlocationOnScreen.getY())) {
                return false;
            }
            return true;
        }
    }

    public void show(Point pt, SourceAndLine source) {
        synchronized (lock) {
            this.source = source;
            File file = new File(source.filePath);
            if (!file.exists()) {
                return;
            }
            try {

                List<String> lines = FileUtils.readLines(file);
                if (lines.size() < source.lineNum) {
                    return;
                }

                int fromLine = 0;
                int toLine = lines.size() - 1;

                lines = lines.subList(fromLine, toLine);

                codeForm = new CodeForm();
                codeForm.initStyles(textArea.getFont(), textArea.getForeground(), textArea.getBackground());
                codeForm.setText(lines, source.lineNum - 1);
                codeForm.updateSize(owner.getHeight() - 40);

                positionPopup(pt);

            } catch (Exception ex) {
//                log.warn("show()", ex);
            }
        }

    }

    public JScrollPane getOwner() {
        return owner;
    }

    public void setOwner(JScrollPane owner) {
        this.owner = owner;
    }

    public SourceAndLine getSource() {
        return source;
    }

    public void setSource(SourceAndLine source) {
        this.source = source;
    }

    public void show(Point pt, String content) {
        synchronized (lock) {
            try {

                codeForm = new CodeForm();
                codeForm.initStyles(textArea.getFont(), textArea.getForeground(), textArea.getBackground());
                codeForm.setText(content);
                codeForm.updateSize(owner.getHeight() - 40);

                positionPopup(pt);

            } catch (Exception ex) {
//                log.warn("show()", ex);
            }
        }
    }

    private void positionPopup(Point pt) throws IllegalArgumentException {
        Point codeFormLocation = new Point();
        Dimension codeFormPreferredSize = codeForm.getPreferredSize();

        Point ownerLocation = owner.getLocationOnScreen();
        Point textAreaScreenLocation = textArea.getLocationOnScreen();
        Dimension textAreaPreferredSize = textArea.getPreferredSize();
        codeFormLocation.x = (int) (textAreaScreenLocation.x + pt.getX());
        codeFormLocation.y = ownerLocation.y + 20;

        if (codeFormLocation.x + codeFormPreferredSize.width >= textAreaScreenLocation.x + textAreaPreferredSize.width
                && codeFormPreferredSize.width < textAreaPreferredSize.width) {
            codeFormLocation.x -= codeFormPreferredSize.width;
            codeFormLocation.x -= 5;
        } else {
            codeFormLocation.x += 5;
        }
        
        if (textAreaPreferredSize.width > codeFormPreferredSize.width) {
            if (codeFormLocation.x < textAreaScreenLocation.x) {
                codeFormLocation.x = textAreaScreenLocation.x;
            }
        }

        PopupFactory pf = PopupFactory.getSharedInstance();
        popup = pf.getPopup(textArea, codeForm, codeFormLocation.x, codeFormLocation.y);
        popup.show();
        codeForm.revalidate();
        codeForm.repaint();
        codeFormlocationOnScreen = codeForm.getLocationOnScreen();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    codeForm.scrollText();
                    codeForm.setFocus();
                } catch (Exception e) {
                }
            }
        });
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

}
