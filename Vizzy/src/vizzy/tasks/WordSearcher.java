/*
 * WordSearcher.java
 *
 * Created on 22 March 2007, 19:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package vizzy.tasks;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import org.apache.log4j.Logger;
import vizzy.comp.JScrollHighlightPanel;
import vizzy.model.Conf;
import vizzy.model.SearchResult;
import vizzy.model.SettingsModel;


/**
 *
 * @author Admin
 */
public class WordSearcher {

    private static final Logger log = Logger.getLogger(WordSearcher.class);
    
    private JTextArea textArea;
    private String word;
    private boolean wasSearching = false;
    private JScrollHighlightPanel highlightPanel;
    private final List<Object> highlightObjects = new ArrayList<Object>();
    private final SettingsModel settings;
    private int nextSearchPos = 0;
    private int lastSearchPos = 0;
    
    public WordSearcher(SettingsModel settings) {
        this.settings = settings;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    public void setHighlightPanel(JScrollHighlightPanel highlightPanel) {
        this.highlightPanel = highlightPanel;
    }

    public String getWord() {
        return word;
    }

    public void clearSearch() {
        nextSearchPos = 0;
        lastSearchPos = 0;
        word = "";
        clearHighlights();
        wasSearching = false;
    }

    // Search for a word and return the offset of the
    // first occurrence. Highlights are added for all
    // occurrences found.
    public synchronized SearchResult search(String keyword, String content, int position) {
//        log.info("Search " + word);
//        new Exception().printStackTrace();

        //System.out.println("word = "+ word);
        clearHighlights();

        wasSearching = true;

        word = keyword;

        if (keyword == null || keyword.equals("")) {
            return null;
        }

        String sWord;
        String sContent;
        if (!settings.isRegexp()) {
            sWord = word.toLowerCase();
            sContent = content.toLowerCase();
        } else {
            sWord = word;
            sContent = content;
        }

        int tmpIndex;
        int wordSize = sWord.length();
        int lastIndex = -1;
        int firstOffset = -1;
        Highlighter highlighter = textArea.getHighlighter();
        ArrayList<Integer> indexes = new ArrayList<Integer>();

        // init regexp
        Pattern pattern = null;
        Matcher matcher = null;
        if (settings.isRegexp()) {
            try {
                pattern = Pattern.compile(sWord);
            } catch (Exception e) {
                return null;
            }
            matcher = pattern.matcher(sContent);
        }


        // find match from current position
        if (settings.isRegexp()) {
            try {
                if (matcher.find(position)) {
                    lastIndex = matcher.start();
                    wordSize = matcher.group().length();
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            lastIndex = sContent.indexOf(sWord, position);
        }

        // if match not found try to search from the beginning
        if (lastIndex == -1) {
            if (settings.isRegexp()) {
                try {
                    if (matcher.find(0)) {
                        lastIndex = matcher.start();
                        wordSize = matcher.group().length();
                    }
                } catch (Exception e) {
                    return null;
                }
            } else {
                lastIndex = sContent.indexOf(sWord);
            }
        }

        // if found
        if (lastIndex != -1) {
            tmpIndex = lastIndex + wordSize;
            try {
                highlightObjects.add(highlighter.addHighlight(lastIndex, tmpIndex, Conf.highlightedSearchResultPainter));
            } catch (Exception e) {
            }
            firstOffset = lastIndex;
        } else {
            if (highlightPanel != null) {
                highlightPanel.setIndexes(indexes);
                highlightPanel.repaint();
            }
            return null;
        }

        if (settings.isHightlightAll()) {

            if (settings.isRegexp()) {
                matcher.reset();
                try {
                    while (matcher.find()) {
                        try {
                            highlightObjects.add(highlighter.addHighlight(matcher.start(), matcher.end(), Conf.searchResultPainter));
                            indexes.add(matcher.start());
                        } catch (BadLocationException e) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    return null;
                }
            } else {
                lastIndex = 0;
                while (true) {
                    lastIndex = sContent.indexOf(sWord, lastIndex);
                    if (lastIndex == -1) {
                        break;
                    }

                    tmpIndex = lastIndex + wordSize;
                    try {
                        highlightObjects.add(highlighter.addHighlight(lastIndex, tmpIndex, Conf.searchResultPainter));
                        indexes.add(lastIndex);
                    } catch (BadLocationException e) {
                        break;
                    }
                    lastIndex = tmpIndex;
                }
            }
        }

        if (highlightPanel != null) {
            highlightPanel.setIndexes(indexes);
            highlightPanel.repaint();
        }
        
        nextSearchPos = firstOffset + wordSize;
        lastSearchPos = firstOffset;

        return new SearchResult(firstOffset, wordSize);
    }

    public boolean isWasSearching() {
        return wasSearching;
    }

    public synchronized void clearHighlights() {
        Highlighter highlighter = textArea.getHighlighter();
        for (Object object : highlightObjects) {
            highlighter.removeHighlight(object);
        }
    }

    public String filter(String keyword, String content) throws Exception {
        wasSearching = true;

        clearHighlights();

        lastSearchPos = 0;
        nextSearchPos = 0;
        word = keyword;
        
        if (word == null || word.equals("")) {
            return "";
        }

        String sWord = word.toLowerCase();
        String sContent = content;

        String[] words = sWord.split(",");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim();
        }

        StringBuilder sb = new StringBuilder("");
        int totalLines = textArea.getLineCount();
        
        for (int i=0; i < totalLines; i++) {
            int start = textArea.getLineStartOffset(i);
            int end = textArea.getLineEndOffset(i);
            String lineText = new String(sContent.substring(start, end));

            for (int j = 0; j < words.length; j++) {
                String w = words[j];
                if (lineText.toLowerCase().indexOf(w) != -1) {
                    sb.append(lineText);
                    break;
                }
            }
        }
        return sb.toString();
    }

    public int getNextSearchPos() {
        return nextSearchPos;
    }

    public int getLastSearchPos() {
        return lastSearchPos;
    }
    
    
    
}
