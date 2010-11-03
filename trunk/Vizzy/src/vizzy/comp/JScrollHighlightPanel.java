/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.comp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;

/**
 *
 * @author sergeil
 */
public class JScrollHighlightPanel extends JPanel {

    private static final Logger log = Logger.getLogger(JScrollHighlightPanel.class);

    private ArrayList<Integer> indexes;
    private JTextArea ta;
    private final static int MARKER_HEIGHT = 3;
    private final static int MARKER_X = 2;
    private boolean allowHighlighting = false;

    public ArrayList<Integer> getIndexes() {
        return indexes;
    }

    public void setIndexes(ArrayList<Integer> indexes) {
        this.indexes = indexes;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (!allowHighlighting) {
            return;
        }

        if (indexes != null) {
            double textAreaHeight = (double)getTa().getHeight();
            double highlightPanelHeight = getHeight() - MARKER_HEIGHT;
            for (Integer integer : indexes) {
                try {
                    Rectangle rect = getTa().modelToView(integer);

                    int lineHeight = (int) (highlightPanelHeight * ((double) rect.getY() / textAreaHeight));

                    int w = getWidth() - 5;
                    int y = lineHeight;

                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(MARKER_X, y, w, MARKER_HEIGHT);
                    g.setColor(Color.BLACK);
                    g.drawRect(MARKER_X, y, w, MARKER_HEIGHT);
                } catch (Exception ex) {
                    log.warn("paint()", ex);
                }
            }
        }
    }

    public JTextArea getTa() {
        return ta;
    }

    public void setTa(JTextArea ta) {
        this.ta = ta;
    }

    public boolean isAllowHighlighting() {
        return allowHighlighting;
    }

    public void setAllowHighlighting(boolean allowHighlighting) {
        this.allowHighlighting = allowHighlighting;
    }
}
