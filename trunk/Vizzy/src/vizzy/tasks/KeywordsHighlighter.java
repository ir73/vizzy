/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.swing.text.Highlighter;
import org.apache.log4j.Logger;
import vizzy.model.HighlightsColorData;
import vizzy.model.SettingsModel;

/**
 *
 * @author sergeil
 */
public class KeywordsHighlighter {

    private static final Logger log = Logger.getLogger(KeywordsHighlighter.class);

    private List<Object> highlightObjects = new ArrayList<Object>();
    private JTextArea textArea;
    private SettingsModel settings;
    
    public KeywordsHighlighter() {
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    public synchronized boolean highlight() {
        Highlighter highlighter = getTextArea().getHighlighter();
        
        clearHighlights();

        int totalLines = getTextArea().getLineCount();
        int start;
        int end;
        String lineText;
        boolean highlighted = false;

        try {
            int i;
            for (i = 0; i < totalLines; i++) {
                start = getTextArea().getLineStartOffset(i);
                end = getTextArea().getLineEndOffset(i);
                lineText = getTextArea().getText(start, end - start);
                for (HighlightsColorData highlight : settings.getHighlightColorData()) {
                    if (lineText.startsWith(highlight.getText())) {
                        highlightObjects.add(highlighter.addHighlight(start, end, highlight.getPainter()));
                        highlighted = true;
                    }
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

    public void setSettingsModel(SettingsModel settings) {
        this.settings = settings;
    }
}
