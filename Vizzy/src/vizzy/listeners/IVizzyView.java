/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.listeners;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import vizzy.comp.JScrollHighlightPanel;

/**
 *
 * @author sergeil
 */
public interface IVizzyView extends ISettingsListener {

    public Rectangle getBounds();

    public Point getLocation();

    public int getHeight();

    public void setIconImage(Image image);

    public JTextArea getTextArea();

    public JScrollHighlightPanel getHighLightScroll();

}
