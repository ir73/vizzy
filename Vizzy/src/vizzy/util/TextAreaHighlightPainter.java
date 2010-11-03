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
public class TextAreaHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

    public TextAreaHighlightPainter(Color color) {
        super(color);
    }
}
