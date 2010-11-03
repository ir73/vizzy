/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.util;

import java.awt.Color;
import javax.swing.text.DefaultHighlighter;

/**
 *
 * @author sergei
 */
public class HighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

    public HighlightPainter(Color color) {
        super(color);
    }
}
