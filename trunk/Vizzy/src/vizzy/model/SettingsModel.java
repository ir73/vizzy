/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import vizzy.forms.panels.SnapshotForm;
import vizzy.listeners.ISettingsListener;
import vizzy.tasks.HandleWordAtPosition;
import vizzy.tasks.KeywordsHighlighter;
import vizzy.tasks.ShowCodePopupTask;
import vizzy.tasks.WordSearcher;
import vizzy.util.DefaultHashMap;

/**
 *
 * @author sergeil
 */
public class SettingsModel {
    private ISettingsListener listener;

    private boolean wasNewFeaturesPanelShown;
    private String defaultFont;
    private Font[] fonts;
    private String flashLogFileName;
    private String policyLogFileName;
    private int logType = 0;
    private String customASEditor = "";
    private boolean isRegexp = false;
    private boolean isDefaultASEditor = true;
    private boolean highlightStackTraceErrors = true;
    private boolean isAutoRefresh = true;
    private boolean isUTF = true;
    private boolean isCheckUpdates = true;
    private boolean maxNumLinesEnabled = false;
    private long maxNumLines = 20;
    private boolean restoreOnUpdate = false;
    private long refreshFreq = 500;
    private boolean isAlwaysOnTop;
    private Date lastUpdateDate = new Date(0L);
    private boolean isHightlightAll = true;
    private boolean isWordWrap = true;
    private boolean isFilter = false;
    private String[] searchKeywords;
    private Font traceFont;
    private String currentLogFile;
    private Rectangle window;
    private DefaultComboBoxModel searchKeywordsModel;
    private String[] fontNames;
    private String recentHash;
    private String traceContent;
    private File settingsFile;
    private boolean isPolicyFileRecorded;
    private Image appIcon;
    private boolean isUIActionsAvailable;
    private boolean enableTraceClick;
    private boolean enableCodePopup;
    private boolean enableParsingSourceLines = false;
    private Map<Integer, String> sourceLines;
    private Color bgColor = Color.white;
    private Color fontColor = Color.black;

    private KeywordsHighlighter keywordsHighlighter;
    private HandleWordAtPosition handleWordAtPosition;
    private ShowCodePopupTask codePopupHandler;
    private WordSearcher searcher;
    private DefaultHashMap<String, String> mmcfgKeys;

    private ArrayList<SnapshotForm> snapshotForms = new ArrayList<SnapshotForm>();

    public SettingsModel() {
        super();
    }

    public void setCustomASEditor(String property, boolean doFireEvent) {
        if (property != null) {
            customASEditor = property;
            if (handleWordAtPosition != null) {
                handleWordAtPosition.setCustomASEditor(this.customASEditor);
            }
            if (doFireEvent && listener != null) {
                getListener().onCustomASEditorChanged(this.customASEditor);
            }
        }
    }

    public void setRefreshFreq(String property, boolean doFireEvent) {
        setRefreshFreq(Long.parseLong(property), doFireEvent);
    }

    public void setLogType(String property, boolean doFireEvent) {
        setLogType(Integer.parseInt(property), doFireEvent);
    }

    public void setHighlightAll(boolean b, boolean doFireEvent) {
        isHightlightAll = b;
        if (doFireEvent && listener != null) {
            getListener().onHightlightAllChanged(this.isHightlightAll);
        }
    }

    public void setWordWrap(boolean b, boolean doFireEvent) {
        isWordWrap = b;
        if (doFireEvent && listener != null) {
            getListener().onWordWrapChanged(this.isWordWrap);
        }
    }

    public void setAutoRefresh(boolean b, boolean doFireEvent) {
        isAutoRefresh = b;
        if (doFireEvent && listener != null) {
            getListener().onAutoRefreshChanged(this.isAutoRefresh);
        }
    }

    public void setFilter(boolean b, boolean doFireEvent) {
        isFilter = b;
        if (doFireEvent && listener != null) {
            getListener().onFilterChanged(this.isFilter);
        }
    }

    public void setSearchKeywords(String[] keywords, boolean doFireEvent) {
        List<String> list = new ArrayList<String>();
        list.add("");
        for (String word : keywords) {
            if (word != null && !"".equals(word)) {
                list.add(word);
            }
        }
        this.searchKeywords = list.toArray(new String[0]);
        this.searchKeywordsModel = new DefaultComboBoxModel(getSearchKeywords());
        if (doFireEvent && listener != null) {
            getListener().onSearchKeywordsChanged(this.searchKeywords, this.searchKeywordsModel);
        }
    }

    public void setTraceFont(String name, String size, boolean doFireEvent) {
        traceFont = new Font(name, 0, Integer.parseInt(size));
        if (doFireEvent && listener != null) {
            getListener().onTraceFontChanged(this.traceFont);
        }
    }

    public void setTraceFont(Font font, boolean doFireEvent) {
        traceFont = font;
        if (doFireEvent && listener != null) {
            getListener().onTraceFontChanged(this.traceFont);
        }
    }

    public void setCurrentLogFile(String filen, boolean doFireEvent) {
        currentLogFile = filen;
        if (doFireEvent && listener != null) {
            getListener().onCurrentLogFileChanged(this.currentLogFile);
        }
    }

    public void setMaxNumLines(String lines, boolean doFireEvent) {
        setMaxNumLines(Long.valueOf(lines), doFireEvent);
    }

    public void setMainWindowLocation(String x, String y, String w, String h, boolean doFireEvent) {
        window = new Rectangle((int)Double.parseDouble(x), (int)Double.parseDouble(y),
                (int)Double.parseDouble(w), (int)Double.parseDouble(h));
        if (doFireEvent && listener != null) {
            getListener().onMainWindowLocationChanged(this.window);
        }
    }

    public void setLastUpdateDate(String last, boolean doFireEvent) {
        if (last != null) {
            Date newDate = new Date(Long.valueOf(last));
            if (lastUpdateDate == null || lastUpdateDate.before(newDate)) {
                setLastUpdateDate(newDate, doFireEvent);
            }
        }
    }

    public Font[] getFonts() {
        return fonts;
    }

    public void setFonts(Font[] fonts, boolean doFireEvent) {
        this.fonts = fonts;
        if (doFireEvent && listener != null) {
            getListener().onFontsChanged(this.fonts);
        }
    }

    public String getFlashLogFileName() {
        return flashLogFileName;
    }

    public void setFlashLogFileName(String flashLogFileName, boolean doFireEvent) {
        this.flashLogFileName = flashLogFileName;
        File f = new File(flashLogFileName);
        if (f.exists() && f.isFile()) {
            int lastIndexOf = f.getAbsolutePath().lastIndexOf(File.separator);
            String dir = "";
            if (lastIndexOf > -1) {
                dir = f.getAbsolutePath().substring(0, lastIndexOf + 1);
            }
            setPolicyLogFileName(dir + Conf.POLICY_LOG_FILE_NAME, doFireEvent);
        }
        if (doFireEvent && listener != null) {
            getListener().onFlashLogFileNameChanged(this.flashLogFileName);
        }
    }

    public String getPolicyLogFileName() {
        return policyLogFileName;
    }

    public void setPolicyLogFileName(String policyLogFileName, boolean doFireEvent) {
        this.policyLogFileName = policyLogFileName;
        if (doFireEvent && listener != null) {
            getListener().onPolicyLogFileNameChanged(this.policyLogFileName);
        }
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType, boolean doFireEvent) {
        this.logType = logType;
        if (doFireEvent && listener != null) {
            getListener().onLogTypeChanged(this.logType);
        }
    }

    public String getCustomASEditor() {
        return customASEditor;
    }

    public boolean isDefaultASEditor() {
        return isDefaultASEditor;
    }

    public void setDefaultASEditor(boolean isDefaultASEditor, boolean doFireEvent) {
        this.isDefaultASEditor = isDefaultASEditor;
        if (handleWordAtPosition != null) {
            handleWordAtPosition.setDefaultEditorUsed(this.isDefaultASEditor);
        }
        if (doFireEvent && listener != null) {
            getListener().onDefaultASEditorChanged(this.isDefaultASEditor);
        }
    }

    public boolean isHighlightStackTraceErrors() {
        return highlightStackTraceErrors;
    }

    public void setHighlightStackTraceErrors(boolean highlightKeywords, boolean doFireEvent) {
        this.highlightStackTraceErrors = highlightKeywords;
        if (doFireEvent && listener != null) {
            getListener().onHighlightStackTraceErrorsChanged(this.highlightStackTraceErrors);
        }
    }

    public boolean isAutoRefresh() {
        return isAutoRefresh;
    }

    public boolean isUTF() {
        return isUTF;
    }

    public void setUTF(boolean isUTF, boolean doFireEvent) {
        this.isUTF = isUTF;
        if (doFireEvent && listener != null) {
            getListener().onUTFChanged(this.isUTF);
        }
    }

    public boolean isCheckUpdates() {
        return isCheckUpdates;
    }

    public void setCheckUpdates(boolean isCheckUpdates, boolean doFireEvent) {
        this.isCheckUpdates = isCheckUpdates;
        if (doFireEvent && listener != null) {
            getListener().onCheckUpdatesChanged(this.isCheckUpdates);
        }
    }

    public boolean isMaxNumLinesEnabled() {
        return maxNumLinesEnabled;
    }

    public void setMaxNumLinesEnabled(boolean maxNumLinesEnabled, boolean doFireEvent) {
        this.maxNumLinesEnabled = maxNumLinesEnabled;
        if (doFireEvent && listener != null) {
            getListener().onMaxNumLinesEnabledChanged(this.maxNumLinesEnabled);
        }
    }

    public long getMaxNumLines() {
        return maxNumLines;
    }

    public void setMaxNumLines(long maxNumLines, boolean doFireEvent) {
        this.maxNumLines = maxNumLines;
        if (doFireEvent && listener != null) {
            getListener().onMaxNumLinesChanged(this.maxNumLines);
        }
    }

    public boolean isRestoreOnUpdate() {
        return restoreOnUpdate;
    }

    public void setRestoreOnUpdate(boolean restoreOnUpdate, boolean doFireEvent) {
        this.restoreOnUpdate = restoreOnUpdate;
        if (doFireEvent && listener != null) {
            getListener().onRestoreOnUpdateChanged(this.restoreOnUpdate);
        }
    }

    public long getRefreshFreq() {
        return refreshFreq;
    }

    public void setRefreshFreq(long refreshFreq, boolean doFireEvent) {
        this.refreshFreq = refreshFreq;
        if (doFireEvent && listener != null) {
            getListener().onRefreshFreqChanged(this.refreshFreq);
        }
    }

    public boolean isAlwaysOnTop() {
        return isAlwaysOnTop;
    }

    public void setAlwaysOnTop(boolean isAlwaysONTop, boolean doFireEvent) {
        this.isAlwaysOnTop = isAlwaysONTop;
        if (doFireEvent && listener != null) {
            getListener().onAlwaysOnTopChanged(this.isAlwaysOnTop);
        }
    }

    public void setAlwaysOnTopUI(boolean isAlwaysOnTop, boolean doFireEvent) {
        if (doFireEvent && listener != null) {
            getListener().onAlwaysOnTopUIChanged(isAlwaysOnTop);
        }
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate, boolean doFireEvent) {
        this.lastUpdateDate = lastUpdateDate;
        if (doFireEvent && listener != null) {
            getListener().onLastUpdateDateChanged(this.lastUpdateDate);
        }
    }

    public String getDefaultFont() {
        return defaultFont;
    }

    public void setDefaultFont(String defaultFont, boolean doFireEvent) {
        this.defaultFont = defaultFont;
        if (doFireEvent && listener != null) {
            getListener().onDefaultFontChanged(this.defaultFont);
        }
    }

    public boolean isHightlightAll() {
        return isHightlightAll;
    }

    public boolean isWordWrap() {
        return isWordWrap;
    }

    public boolean isFilter() {
        return isFilter;
    }

    public String[] getSearchKeywords() {
        return searchKeywords;
    }

    public Font getTraceFont() {
        return traceFont;
    }

    public String getCurrentLogFile() {
        return currentLogFile;
    }

    public DefaultComboBoxModel getSearchKeywordsModel() {
        return searchKeywordsModel;
    }

    public Rectangle getMainWindowLocation() {
        return window;
    }

    public void setFontNames(String[] fontNames, boolean doFireEvent) {
        this.fontNames = fontNames;
        if (doFireEvent && listener != null) {
            getListener().onSetFontNamesChanged(this.fontNames);
        }
    }

    public String[] getFontNames() {
        return fontNames;
    }

    public String getRecentHash() {
        return recentHash;
    }

    public void setRecentHash(String recentHash, boolean doFireEvent) {
        this.recentHash = recentHash;
        if (doFireEvent && listener != null) {
            getListener().onRecentHashChanged(this.recentHash);
        }
    }

    public String getTraceContent() {
        return traceContent;
    }

    public void setTraceContent(String traceContent, boolean doFireEvent) {
        this.traceContent = traceContent;
        if (doFireEvent && listener != null) {
            getListener().onTraceContentChanged(this.traceContent);
        }
    }

    public ISettingsListener getListener() {
        return listener;
    }

    public void setListener(ISettingsListener listener) {
        this.listener = listener;
    }

    public void setPolicyFileRecorded(boolean policyFileRecorded) {
        this.isPolicyFileRecorded = policyFileRecorded;
    }

    public boolean isPolicyFileRecorded() {
        return isPolicyFileRecorded;
    }

    public KeywordsHighlighter getKeywordsHighlighter() {
        return keywordsHighlighter;
    }

    public void setKeywordsHighlighter(KeywordsHighlighter keywordsHighlighter) {
        this.keywordsHighlighter = keywordsHighlighter;
    }

    public HandleWordAtPosition getHandleWordAtPosition() {
        return handleWordAtPosition;
    }

    public void setHandleWordAtPosition(HandleWordAtPosition handleWordAtPosition) {
        this.handleWordAtPosition = handleWordAtPosition;
    }

    public ShowCodePopupTask getCodePopupHandler() {
        return codePopupHandler;
    }

    public void setCodePopupHandler(ShowCodePopupTask codePopupHandler) {
        this.codePopupHandler = codePopupHandler;
    }

    public WordSearcher getSearcher() {
        return searcher;
    }

    public void setSearcher(WordSearcher searcher) {
        this.searcher = searcher;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public void setSettingsFile(File settingsFile) {
        this.settingsFile = settingsFile;
    }

    public Image getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Image appIcon) {
        this.appIcon = appIcon;
    }

    public ArrayList<SnapshotForm> getSnapshotForms() {
        return snapshotForms;
    }

    public void setSnapshotForms(ArrayList<SnapshotForm> snapshotForms) {
        this.snapshotForms = snapshotForms;
    }

    public DefaultHashMap<String, String> getMmcfgKeys() {
        return mmcfgKeys;
    }

    public void setMmcfgKeys(DefaultHashMap<String, String> mmcfgKeys) {
        this.mmcfgKeys = mmcfgKeys;
    }

    public boolean isUIActionsAvailable() {
        return isUIActionsAvailable;
    }

    public void setUIActionsAvailable(boolean isUIActionsAvailable) {
        this.isUIActionsAvailable = isUIActionsAvailable;
    }

    public void search(String word, SearchResult searchResult, boolean scrollToSearchResult) {
        if (listener != null) {
            listener.onSearch(word, searchResult, scrollToSearchResult);
        }
    }

    public void highlightTraceKeyword(String text) {
        if (listener != null) {
            listener.onHighlightTraceKeyword(text);
        }
    }

    public void optionsClosed() {
        if (listener != null) {
            listener.onOptionsClosed();
        }
    }

    public void clearTrace(boolean doFireEvent) {
        setTraceContent("", doFireEvent);
        setRecentHash(null, doFireEvent);
    }

    public void clearSearch() {
        if (listener != null) {
            listener.onSearchCleared();
        }
    }

    public void onInit() {
        if (listener != null) {
            listener.onInit();
        }
    }

    public void onAfterInit() {
        if (listener != null) {
            listener.onAfterInit();
        }
    }

    public void setMainWindowLocation(Rectangle bounds, boolean doFireEvent) {
        window = bounds;
        if (doFireEvent && listener != null) {
            getListener().onMainWindowLocationChanged(this.window);
        }
    }

    public boolean isEnableTraceClick() {
        return enableTraceClick;
    }

    public void setEnableTraceClick(boolean enableStackTraceClick, boolean doFireEvent) {
        this.enableTraceClick = enableStackTraceClick;
        if (doFireEvent && listener != null) {
            getListener().onEnableTraceClickChanged(this.enableTraceClick);
        }
    }

    public boolean isEnableCodePopup() {
        return enableCodePopup;
    }

    public void setEnableCodePopup(boolean enableCodePopup, boolean doFireEvent) {
        this.enableCodePopup = enableCodePopup;
        if (doFireEvent && listener != null) {
            getListener().onEnableCodePopupChanged(this.enableCodePopup);
        }
    }

    public void showNewFeaturesPanel() {
        if (listener != null) {
            getListener().onShowNewFeaturesPanel();
        }
    }

    public boolean wasNewFeaturesPanelShown() {
        return wasNewFeaturesPanelShown;
    }

    public void setNewFeaturesPanelShown(boolean wasNewFeaturesPanelShown, boolean doFireEvent) {
        this.wasNewFeaturesPanelShown = wasNewFeaturesPanelShown;
        if (doFireEvent && listener != null) {
            getListener().onNewFeaturesPanelShownChanged(this.wasNewFeaturesPanelShown);
        }
    }

    public boolean isRegexp() {
        return isRegexp;
    }

    public void setRegexp(boolean isRegexp, boolean doFireEvent) {
        this.isRegexp = isRegexp;
        if (doFireEvent && listener != null) {
            getListener().onRegexpChanged(this.isRegexp);
        }
    }

    public void beforeFilter() {
        if (listener != null) {
            listener.beforeFilter();
        }
    }

    public void afterFilter(String content) {
        if (listener != null) {
            listener.afterFilter(content);
        }
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor, boolean doFireEvent) {
        this.bgColor = bgColor;
        if (doFireEvent && listener != null) {
            getListener().onBgColorChanged(this.bgColor);
        }
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor, boolean doFireEvent) {
        this.fontColor = fontColor;
        if (doFireEvent && listener != null) {
            getListener().onFontColorChanged(this.fontColor);
        }
    }

    public void setFontColor(String property, boolean b) {
        if (property != null) {
            setFontColor(new Color(Integer.parseInt(property)), b);
        }
    }

    public void setBgColor(String property, boolean b) {
        if (property != null) {
            setBgColor(new Color(Integer.parseInt(property)), b);
        }
    }

    public Map<Integer, String> getSourceLines() {
        return sourceLines;
    }

    public void setSourceLines(Map<Integer, String> sourceLines) {
        this.sourceLines = sourceLines;
    }

    public boolean isEnableParsingSourceLines() {
        return enableParsingSourceLines;
    }

    public void setEnableParsingSourceLines(boolean enableParsingSourceLines, boolean doFireEvent) {
        this.enableParsingSourceLines = enableParsingSourceLines;
        if (doFireEvent && listener != null) {
            getListener().onEnableParsingSourceLines(this.enableParsingSourceLines);
        }
    }
    

}
