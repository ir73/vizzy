/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.tasks;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import vizzy.model.Conf;
import vizzy.model.SettingsModel;
import vizzy.model.SourceAndLine;

/**
 *
 * @author sergeil
 */
public class HandleWordAtPosition {

    private static final Logger log = Logger.getLogger(HandleWordAtPosition.class);
    private final SettingsModel settings;

    public HandleWordAtPosition(SettingsModel settings) {
        this.settings = settings;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    class MinPositionParam {
        public String separator;
        public boolean substract;
        public MinPositionParam(String keyword, boolean substract) {
            this.separator = keyword;
            this.substract = substract;
        }
    }

    public static final String HTTPS_TEMPLATE = "https://";
    public static final String HTTP_TEMPLATE = "http://";
    public static final String FILE_TEMPLATE = "file:///";
    private String customASEditor;
    private boolean defaultASEditor;
    private JTextArea textArea;
    private Object highlight;
    private final Object lock = new Object();

    public boolean findObjectAtPositionAndExecute(int offset) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }

        try {
            if (checkHTTPLink(offset, true) != null) {
                return true;
            }
        } catch (Exception ex) {
//            log.warn("findObjectAtPositionAndExecute() checkHTTPLink failed", ex);
        }

        try {
            if (checkSourceFile(offset, true) != null) {
                return true;
            }
        } catch (Exception ex) {
//            log.warn("findObjectAtPositionAndExecute() checkSourceFile failed", ex);
        }

        return false;
    }

    public void removeHighlight() {
        synchronized (lock) {
            try {
                if (highlight != null) {
                    textArea.getHighlighter().removeHighlight(highlight);
                    highlight = null;
                }
            } catch (Exception e) {
//                log.warn("removeHighlight()", e);
            }
        }
    }

    public void highlightSourceLine(SourceAndLine source) {
        removeHighlight();
        if (source.startPos > -1) {
            synchronized (lock) {
                try {
                    highlight = textArea.getHighlighter().addHighlight(source.startPos, source.startPos + source.filePath.length(), Conf.mouseOverObjectPainter);
                } catch (BadLocationException ex) {
//                log.warn("highlightSourceLine()", ex);
                }
            }
        }
    }

    public SourceAndLine checkHTTPLink(int offset, boolean executeIfFound) throws Exception {

        int startIndex;
        int endIndex;
        String currentWord;

        int currentIndex = offset;
        String text = textArea.getText();

        // CHECK FOR HTTP LINK
        startIndex = getMinPosition(text, currentIndex,
                new MinPositionParam[]{
                    new MinPositionParam(HTTPS_TEMPLATE, false),
                    new MinPositionParam(HTTP_TEMPLATE, false),
                    new MinPositionParam(FILE_TEMPLATE, false),
                    new MinPositionParam("\n", true),
                    new MinPositionParam("\r", true),
                    new MinPositionParam(" ", true)},
                true);
        endIndex = getMinPosition(text, currentIndex,
                new MinPositionParam[]{
                    new MinPositionParam("\n", true),
                    new MinPositionParam("\r", true),
                    new MinPositionParam("\"", true),
                    new MinPositionParam("'", true),
                    new MinPositionParam("<", true),
                    new MinPositionParam(">", true),
                    new MinPositionParam(")", true),
                    new MinPositionParam(" ", true)},
                false);

        if (endIndex != -1 && startIndex != -1) {
            currentWord = text.substring(startIndex, endIndex);
            if (currentWord != null
                    && (currentWord.startsWith(HTTP_TEMPLATE) || currentWord.startsWith(FILE_TEMPLATE))) {
                SourceAndLine source = new SourceAndLine(currentWord, -1, startIndex);
                highlightSourceLine(source);
                if (executeIfFound) {
                    Desktop.getDesktop().browse(new URI(currentWord));
                }
                return source;
            }
        }
        return null;
    }

    public SourceAndLine checkSourceFile(int offset, boolean executeIfFound) throws Exception {
        SourceAndLine source = extractSourceFile(offset, textArea.getText(), 
                textArea.getLineOfOffset(offset), textArea.getLineCount());
        if (source == null) {
            return null;
        }

        highlightSourceLine(source);
        
        if (executeIfFound) {
            if (defaultASEditor) {
                File file = new File(source.filePath);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                }
            } else if (customASEditor != null) {
                String command = customASEditor.replace("%file%", source.filePath).replace("%line%", String.valueOf(source.lineNum));
                Runtime.getRuntime().exec(command);
            }
        }

        return source;
    }

    public String checkJSON(int offset) throws Exception {
        String selectedText = textArea.getSelectedText();

        if (selectedText == null || selectedText.isEmpty()) {
            return null;
        }

        if (textArea.getSelectionStart() > offset
                || textArea.getSelectionEnd() < offset) {
            return null;
        }

        JSONObject parse = null;
        try {
            parse = (JSONObject) new JSONParser().parse(selectedText);
        } catch (Exception e) {

        }
        if (parse == null) {
            return null;
        }
        JSONObject.inline = 0;
        String toJSONString = parse.toJSONString();
        return toJSONString;
    }

    private SourceAndLine extractSourceFile(int currentIndex, String text, int taLineNum, int taTotalLines) {
        int startIndex;
        int endIndex;
        String currentWord;

        startIndex = getMinPosition(text, currentIndex,
                new MinPositionParam[]{
                    new MinPositionParam("\n", true),
                    new MinPositionParam("\r", true)},
                true);
        endIndex = getMinPosition(text, currentIndex,
                new MinPositionParam[]{
                    new MinPositionParam("\n", true),
                    new MinPositionParam("\r", true)},
                false);

        if (endIndex == -1
                && taLineNum == taTotalLines) {
            endIndex = text.length() + 10;
        }

        if (startIndex == -1
                && taLineNum == 0) {
            startIndex = 0;
        }

        if (endIndex != -1 && startIndex != -1
                && startIndex < endIndex) {
            currentWord = text.substring(startIndex, endIndex); // whole line

            // check stack trace
            if (currentWord != null
                    && currentWord.startsWith("\tat ")
                    && currentWord.endsWith("]")) {
                int sIndex = currentWord.indexOf("[");
                int eIndex = currentWord.lastIndexOf(":");
                if (sIndex != -1 && eIndex != -1
                        && sIndex < eIndex

                && currentIndex > startIndex + sIndex
                && currentIndex < startIndex + eIndex) {
                    sIndex = sIndex + 1;
                    String filePath = currentWord.substring(sIndex, eIndex);
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            int lineNum = Integer.parseInt(currentWord.substring(eIndex + 1, currentWord.length() - 1));
                            return new SourceAndLine(filePath, lineNum, startIndex + sIndex);
                        }
                    }
                }
                
            }
            // check Vizzy Plugin
            if (settings.getSourceLines() != null
                    && settings.getSourceLines().containsKey(taLineNum)) {
                String debugLine = settings.getSourceLines().get(taLineNum);
                if (debugLine != null && debugLine.length() > 0) {
                    int openBrace = debugLine.indexOf("[");
                    if (openBrace != -1) {
                        int semiColIndex = debugLine.lastIndexOf(":");
                        if (semiColIndex != -1) {
                            int closingBrace = debugLine.indexOf("]");
                            if (closingBrace != -1) {
                                return new SourceAndLine(debugLine.substring(openBrace + 1, semiColIndex).trim(),
                                        Integer.parseInt(debugLine.substring(semiColIndex + 1, closingBrace).trim()), -1);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private int getMinPosition(String text, int currentIndex, MinPositionParam[] minPositionParam, boolean left) {
        int index = -1;
        for (MinPositionParam param : minPositionParam) {
            int i;
            if (left)
                i = text.lastIndexOf(param.separator, currentIndex);
            else
                i = text.indexOf(param.separator, currentIndex);
            if (i != -1) {
                if (left) {
                    if (param.substract)
                        i = i + param.separator.length();
                    if (index == -1)
                        index = i;
                    else if (index < i)
                        index = i;
                } else {
                    if (index == -1)
                        index = i;
                    else if (index > i)
                        index = i;
                }
            }
        }
        return index;
    }

    public void setCustomASEditor(String customASEditor) {
        this.customASEditor = customASEditor;
    }

    public void setDefaultEditorUsed(boolean defaultASEditor) {
        this.defaultASEditor = defaultASEditor;
    }

}
