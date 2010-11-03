/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.swing.text.Highlighter;
import org.apache.log4j.Logger;
import vizzy.model.Conf;

/**
 *
 * @author sergeil
 */
public class KeywordsHighlighter {

    private static final Logger log = Logger.getLogger(KeywordsHighlighter.class);

    private static final Pattern templateError = Pattern.compile("\\bError #[0-9]+:");
    private List<Object> highlightObjects = new ArrayList<Object>();

    public KeywordsHighlighter() {
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    private JTextArea textArea;

    public synchronized boolean highlight() {
        Highlighter highlighter = getTextArea().getHighlighter();
        
        clearHighlights();

        int totalLines = getTextArea().getLineCount();
        int start;
        int end;
        int ind;
        String lineText;
        boolean highlighted = false;

        try {
            int indEnd;
            int i;
            Matcher matcher;
            for (i = 0; i < totalLines; i++) {
                start = getTextArea().getLineStartOffset(i);
                end = getTextArea().getLineEndOffset(i);
                lineText = getTextArea().getText(start, end - start);
                matcher = templateError.matcher(lineText);
                if (matcher.find()) {
                    highlightObjects.add(highlighter.addHighlight(start + matcher.start(), start + matcher.end(), Conf.errorPainter));
                    highlighted = true;
                }
            }
        } catch (Exception e) {
            log.warn("highlight() ", e);
        }
        return highlighted;
    }

    public synchronized void clearHighlights() {
        Highlighter highlighter = getTextArea().getHighlighter();
        for (Object object : highlightObjects) {
            highlighter.removeHighlight(object);
        }
    }
}
