/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.model;

import java.awt.Color;
import javax.swing.text.Highlighter.HighlightPainter;
import vizzy.util.TextAreaHighlightPainter;

/**
 *
 * @author user
 */
public class HighlightsColorData {
    private int i;
    private String text;
    private Color background;
    private HighlightPainter painter;

    public HighlightsColorData(int i, String text, Color background) {
        this.i = i;
        this.text = text;
        setBackground(background);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
        this.painter = new TextAreaHighlightPainter(background);
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public HighlightPainter getPainter() {
        return painter;
    }
}
