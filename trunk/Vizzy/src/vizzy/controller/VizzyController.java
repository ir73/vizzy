/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.controller;

import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import vizzy.forms.VizzyForm;
import vizzy.forms.panels.AboutPanel;
import vizzy.forms.panels.OptionsForm;
import vizzy.forms.panels.SnapshotForm;
import vizzy.listeners.ILogFileListener;
import vizzy.listeners.IUpdateCheckListener;
import vizzy.listeners.IVizzyView;
import vizzy.listeners.OutOfMemoryDisplayedListener;
import vizzy.model.Conf;
import vizzy.model.FlashPlayerFiles;
import vizzy.model.SearchResult;
import vizzy.model.SettingsModel;
import vizzy.model.SourceAndLine;
import vizzy.tasks.CheckLogReadTime;
import vizzy.tasks.CheckUpdates;
import vizzy.tasks.DebugPlayerDetector;
import vizzy.tasks.DeleteFile;
import vizzy.tasks.FlashPlayerFilesLocator;
import vizzy.tasks.FontsInitializer;
import vizzy.tasks.HandleWordAtPosition;
import vizzy.tasks.HideCodePopupTimerTask;
import vizzy.tasks.KeywordsHighlighter;
import vizzy.tasks.LoadFileTask;
import vizzy.tasks.MMCFGInitializer;
import vizzy.tasks.ShowCodePopupTask;
import vizzy.tasks.ShowOutOfMemMessage;
import vizzy.tasks.WordSearcher;
import vizzy.util.OSXAdapter;
import vizzy.util.PathUtils;
import vizzy.util.TextTransfer;

/**
 *
 * @author sergeil
 */
public final class VizzyController implements ILogFileListener {

    private static final Logger log = Logger.getLogger(VizzyController.class.getName());
    private static IVizzyView view;
    private static VizzyController controller;
    private SettingsModel settings;
    private MMCFGInitializer mmcfgInitializer;
    private CheckUpdates checkUpdatesThread;
    private Timer hideCodePopupTimer;
    private Properties props;
    private OptionsForm optionsForm;
    private AboutPanel aboutForm;
    private ShowOutOfMemMessage showOutOfMemMessage;
    private ScheduledExecutorService readFilescheduler;

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    VizzyController.controller = new VizzyController();
                } catch (Exception ex) {
                    log.error("main()", ex);
                }
            }
        });
    }

    public VizzyController() {
        super();
        start();
    }

    private String parseLogSourceData(String log) {
//        long currentTimeMillis = System.currentTimeMillis();

        Map<Integer, String> sourceLines = new ConcurrentHashMap<Integer, String>();

        List<String> list = new ArrayList<String>();
        int lastIndex = 0;
        int curentIndex = -1;
        while ((curentIndex = log.indexOf("\n", lastIndex)) != -1) {
            list.add(log.substring(lastIndex, curentIndex));
            lastIndex = curentIndex + 1;
        }

        list.add(log.substring(lastIndex));

//        long currentTimeMillis1 = System.currentTimeMillis();
//        System.out.println(currentTimeMillis1 - currentTimeMillis);

        StringBuilder sb = new StringBuilder();
        int lines = 0;
        int len = list.size();
        String s;
        for (int i = 0; i < len; i++) {
            s = list.get(i);
            if (s.startsWith("|vft|")) {
                sourceLines.put(lines, s);
            } else {
                if (lines > 0) {
                    sb.append("\n");
                }
                sb.append(s);
                lines++;
            }
        }
        settings.setSourceLines(sourceLines);
        return sb.toString();
    }

    private void start() {
        initUIManager();
        settings = new SettingsModel();
        view = new VizzyForm(this, settings);
        settings.setListener(view);
        settings.setUIActionsAvailable(false);

        settings.onInit();
        init();
    }

    private void init() {
        preInit();
        initSystemFonts();
        initFlashLog();
        initMMCFG();
        loadProperties();
        initVars();
        initSettings(view.getBounds());
        initCurrentLogTimer();
        initCheckUpdates();
        initNewFeatures();
        initKeyBindings();
        if (!(new File(settings.getCurrentLogFile()).exists())) {
            settings.setTraceContent("Seems that your Flash Player is not ready to debug. "
                    + "Click Extra -> Detect Flash Player in the menu and follow instructions.", true);
        }
        settings.setUIActionsAvailable(true);
        settings.onAfterInit();
    }

    private void initUIManager() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            log.error("initUIManager()", ex);
        }
    }

    private void initNewFeatures() {
        if (!settings.isFirstRun() && !settings.wasNewFeaturesPanelShown()) {
            settings.showNewFeaturesPanel();
        }
    }

    /**
     * Inites UI before view components are created
     * and packed
     */
    private void preInit() {
        if (Conf.OS_MAC_OS_X.equals(Conf.OSName)) {
            try {
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("onClose", (Class[]) null));
            } catch (Exception ex) {
                log.warn("setQuitHandler()", ex);
            }
        }

        String rootDir = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            rootDir = URLDecoder.decode(rootDir, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.warn("Unable to decode root dir 1. " + rootDir, ex);
        }
        File dir = PathUtils.getDir(new File(rootDir));
        Conf.vizzyRootDir = dir.getAbsolutePath();

        log.info("rootDir = " + rootDir);
        log.info("vizzyRootDir = " + Conf.vizzyRootDir);

        if (Conf.vizzyRootDir == null || Conf.vizzyRootDir.equals("")) {
            Conf.vizzyRootDir = ".";
        }

        if (Conf.OSName.indexOf(Conf.OS_WINDOWS) > -1) {
            settings.setCustomASEditor(Conf.DEFAULT_WINDOWS_EDITOR_PATH, true);
        } else if (Conf.OSName.indexOf(Conf.OS_MAC_OS_X) > -1) {
            settings.setCustomASEditor(Conf.DEFAULT_MAC_EDITOR_PATH, true);
        } else if (Conf.OSName.indexOf(Conf.OS_LINUX) > -1) {
            settings.setCustomASEditor(Conf.DEFAULT_LINUX_EDITOR_PATH, true);
        }

        ToolTipManager.sharedInstance().setInitialDelay(0);

        settings.setSettingsFile(new File(Conf.vizzyRootDir, Conf.VIZZY_PROPERTIES_FILENAME));
        if (!settings.getSettingsFile().exists()) {
            settings.setFirstRun(true);
        }

        try {
            URL myIconUrl = this.getClass().getResource("/img/vizzy.png");
            settings.setAppIcon(new ImageIcon(myIconUrl, "Vizzy Flash Tracer").getImage());
            view.setIconImage(settings.getAppIcon());
        } catch (Exception e) {
//            log.warn("setAppIcon()", e);
        }
    }

    /**
     * Loads system fonts found on machine
     */
    private void initSystemFonts() {
        FontsInitializer f = new FontsInitializer();
        f.loadSystemFonts();
        settings.setFonts(f.getFonts(), false);
        settings.setFontNames(f.getFontNames(), false);
    }

    /**
     * Get flashlog.txt file location depending on user's
     * operation system
     */
    private void initFlashLog() {
        FlashPlayerFiles fpf = FlashPlayerFilesLocator.findFilesPaths();
        if (fpf.getLogPath() != null) {
            settings.setFlashLogFileName(fpf.getLogPath(), false);
        }
    }

    /**
     * Create mm.cfg file if necessary or
     * get flashlog.txt file location if it's written
     * in mm.cfg
     */
    private void initMMCFG() {
        mmcfgInitializer = new MMCFGInitializer();
        mmcfgInitializer.init();
        settings.setMmcfgKeys(mmcfgInitializer.getMmcfgKeys());
        settings.setPolicyFileRecorded(mmcfgInitializer.isPolicyFileRecorded());
//        if (mmcfgInitializer.isMmcfgCreated()) {
//            JOptionPane.showMessageDialog(null, "Vizzy has created mm.cfg file for you.\n" +
//                    "Please restart all your browsers for the\n"
//                    + "changes to take effect.",
//                    "Info", JOptionPane.INFORMATION_MESSAGE);
//        }
        if (mmcfgInitializer.getTraceFileLocation() != null) {
            settings.setFlashLogFileName(mmcfgInitializer.getTraceFileLocation(), false);
        }

    }

    /**
     * Loads settings file
     */
    private void loadProperties() {
        props = new Properties();
        try {
            props.load(new FileInputStream(settings.getSettingsFile()));
        } catch (Exception ex) {
//            log.warn("loadProperties()", ex);
        }
    }

    private void initVars() {
        settings.setSearcher(new WordSearcher(settings));
        settings.setKeywordsHighlighter(new KeywordsHighlighter());
        settings.setHandleWordAtPosition(new HandleWordAtPosition(settings));
        settings.setCodePopupHandler(new ShowCodePopupTask());

        settings.getSearcher().setTextArea(view.getTextArea());
        settings.getSearcher().setHighlightPanel(view.getHighLightScroll());
        settings.getKeywordsHighlighter().setTextArea(view.getTextArea());
        settings.getHandleWordAtPosition().setTextArea(view.getTextArea());
        settings.getCodePopupHandler().setTextArea(view.getTextArea());
        settings.getCodePopupHandler().setOwner(view.getScrollPane());

        String defaultFont = "Courier New";
        Font[] fonts = settings.getFonts();
        for (Font font : fonts) {
            if (font.getName().toLowerCase().indexOf("courier") > -1) {
                defaultFont = font.getName();
                break;
            }
        }
        settings.setDefaultFont(defaultFont, false);
    }

    private void initCheckUpdates() {
        if (settings.isCheckUpdates()) {
            Date nowDate = new Date();
            Date lastUpdateDate = settings.getLastUpdateDate();
            Date weekAgoDate = new Date(nowDate.getTime() - Conf.UPDATE_CHECK_FREQ);
            int diff = weekAgoDate.compareTo(lastUpdateDate);
            if (diff > 0) {
                settings.setLastUpdateDate(nowDate, false);
                checkUpdatesThread = new CheckUpdates(new IUpdateCheckListener() {
                    public void exit() {
                        saveBeforeExit();
                    }
                });
                checkUpdatesThread.start();
            }
        }
    }

    private void initCurrentLogTimer() {
        createReadLogTimerTask().run();
        if (settings.isAutoRefresh()) {
            startReadLogFileTimer();
        } else {
            stopReadLogFileTimer();
        }
    }

    private LoadFileTask createReadLogTimerTask() {
        return new LoadFileTask(settings, this);
    }

    public void startReadLogFileTimer() {
        stopReadLogFileTimer();
        readFilescheduler = Executors.newSingleThreadScheduledExecutor();
        readFilescheduler.scheduleWithFixedDelay(createReadLogTimerTask(), 1000, settings.getRefreshFreq(), TimeUnit.MILLISECONDS);
    }

    public void stopReadLogFileTimer() {
        if (readFilescheduler != null && !readFilescheduler.isShutdown()) {
            readFilescheduler.shutdown();
        }
    }

    private void initSettings(Rectangle rect) {
        settings.setLastUpdateDate(props.getProperty("settings.update.last"), true);
        settings.setCheckUpdates(props.getProperty("settings.update.autoupdates", "true").equals("true"), true);
        settings.setFlashLogFileName(props.getProperty("settings.flashlog.filename", settings.getFlashLogFileName()), true);
        settings.setRefreshFreq(props.getProperty("settings.refresh_freq", "500"), true);
        settings.setUTF(props.getProperty("settings.flashlog.utf", "true").equals("true"), true);
        settings.setMaxNumLinesEnabled(props.getProperty("settings.flashlog.max_num_lines_enabled", "false").equals("true"), true);
        settings.setMaxNumLines(props.getProperty("settings.flashlog.max_num_lines", "50000"), true);
        settings.setRestoreOnUpdate(props.getProperty("settings.restore_on_trace_update", "false").equals("true"), true);
        settings.setAlwaysOnTop(props.getProperty("settings.always_on_top", "false").equals("true"), true);
        settings.setHighlightAll(props.getProperty("settings.highlight_all", "true").equals("true"), true);
        settings.setAutoRefresh(props.getProperty("settings.flashlog.auto_refresh", "true").equals("true"), true);
        settings.setWordWrap(props.getProperty("settings.flashlog.word_wrap", "true").equals("true"), true);
        settings.setEnableParsingSourceLines(props.getProperty("settings.flashlog.vizzy_trace_enabled", "false").equals("true"), true);
        settings.setHighlightStackTraceErrors(props.getProperty("settings.flashlog.highlight_errors_enabled", "false").equals("true"), true);
        settings.setEnableCodePopup(props.getProperty("settings.enable_code_popups", "true").equals("true"), true);
        settings.setEnableTraceClick(props.getProperty("settings.enable_trace_click", "true").equals("true"), true);
        settings.setCustomASEditor(props.getProperty("settings.custom_as_editor", null), true);
        settings.setSearchVisible(props.getProperty("settings.search_panel_visible", "true").equals("true"), true);
        settings.setLineNumbersVisible(props.getProperty("settings.line_numbers_visible", "true").equals("true"), true);

        settings.setNewFeaturesPanelShown(props.getProperty("settings.new_features_shown" + Conf.VERSION, "false").equals("true"), true);
        settings.setTraceFont(props.getProperty("settings.font.name", settings.getDefaultFont()),
                props.getProperty("settings.font.size", "12"), true);
        settings.setFontColor(props.getProperty("settings.font.color"), true);
        settings.setBgColor(props.getProperty("settings.text_area.color"), true);
        settings.setSearchKeywords(props.getProperty("search.keywords", "").split("\\|\\|\\|"), true);
        settings.setFilter(false, true);
        setLogType(props.getProperty("settings.log_type", "0"), true);
        settings.setMainWindowLocation(props.getProperty("settings.window.x", String.valueOf(rect.getX())),
                props.getProperty("settings.window.y", String.valueOf(rect.getY())),
                props.getProperty("settings.window.width", String.valueOf(rect.getWidth())),
                props.getProperty("settings.window.height", String.valueOf(rect.getHeight())), true);

        settings.setDefaultASEditor(props.getProperty("settings.use_custom_as_editor", "true").equals("true"), true);

        if (settings.isFirstRun()) {
            if (Conf.OSName.indexOf(Conf.OS_WINDOWS) > -1) {
                if (settings.getCustomASEditor() != null) {
                    File f = new File(Conf.FLASHDEVELOP_PATH);
                    if (f.exists()) {
                        settings.setDefaultASEditor(false, true);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void onLogFileRead(String log) {
        int len = log.length();
        int max = len > 500 ? len - 500 : 0;
        String currentHash = len + "" + log.substring(max, len);

        if (currentHash.equals(settings.getRecentHash())) {
            return;
        }

        CheckLogReadTime check = new CheckLogReadTime(this, Conf.MAX_TIMER_BEFORE_OUTOFMEMORY);
        check.start();

        if (settings.isEnableParsingSourceLines()) {
            log = parseLogSourceData(log);
        }

        settings.setRecentHash(currentHash, false);
        settings.setTraceContent(log, true);

        if (settings.getSearcher().isWasSearching()) {
            startSearch(settings.getSearcher().getLastSearchPos(), false);
        }
        highlightStackTraceErrors();

        check.stopRunning();
    }

    @Override
    public synchronized void onOutOfMemory() {
        if (settings.isMaxNumLinesEnabled() && settings.getMaxNumLines() <= Conf.MAX_NUM_LINES_OUTOFMEMORY) {
            return;
        }

        settings.setMaxNumLinesEnabled(true, false);
        settings.setMaxNumLines(Conf.MAX_NUM_LINES_OUTOFMEMORY, false);

        stopReadLogFileTimer();

        showOutOfMemMessage = new ShowOutOfMemMessage(new OutOfMemoryDisplayedListener() {

            @Override
            public void messageDisplayed() {
                JOptionPane.showMessageDialog(null, "The log file is too big and Vizzy has\n" + "run out of memory. Vizzy has set the limit\n" + "of log file to 50KB. You can customize this\n" + "value in Options menu.", "Warning", JOptionPane.ERROR_MESSAGE);
                createReadLogTimerTask().run();
                if (settings.isAutoRefresh()) {
                    startReadLogFileTimer();
                }
            }
        });
        showOutOfMemMessage.start();
    }

    private void highlightStackTraceErrors() {
        if (settings.isHighlightStackTraceErrors()) {
            settings.getKeywordsHighlighter().highlight();
        }
    }

    private boolean handleWordAtPosition(int offset) {
        if (settings.isEnableTraceClick()) {
            return settings.getHandleWordAtPosition().findObjectAtPositionAndExecute(offset);
        }
        return false;
    }

    public void startHideCodePopupTimer() {
        if (!settings.isEnableCodePopup()) {
            return;
        }
        stopHideCodePopupTimer();
        hideCodePopupTimer = new Timer("startHideCodePopupTimer", true);
        hideCodePopupTimer.schedule(new HideCodePopupTimerTask(this), 500, 500);
    }

    public void stopHideCodePopupTimer() {
        if (!settings.isEnableCodePopup()) {
            return;
        }
        if (hideCodePopupTimer != null) {
            hideCodePopupTimer.cancel();
            hideCodePopupTimer = null;
        }
    }

    public void hideCodePopup() {
        if (!settings.isEnableCodePopup()) {
            return;
        }
        if (!settings.getCodePopupHandler().isVisible()) {
            return;
        }
        settings.getHandleWordAtPosition().removeHighlight();
        settings.getCodePopupHandler().hide();
    }

    public void onHideCodePopup() {
        if (!settings.isEnableCodePopup()) {
            return;
        }
        stopHideCodePopupTimer();
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        if (mouseLocation == null || !settings.getCodePopupHandler().isMouseAtCodePopup(mouseLocation)) {
            hideCodePopup();
        }
    }

    private void setLogType(String property, boolean uiTrigger) {
        settings.setLogType(property, uiTrigger);
        if (settings.getLogType() == 1) {
            if (!settings.isPolicyFileRecorded()) {
                mmcfgInitializer.recordPolicyFile();
                settings.setPolicyFileRecorded(true);
                JOptionPane.showMessageDialog(null, "Vizzy has updated mm.cfg file to enable\n"
                        + "policy logging. Please restart all your browsers\n"
                        + "for the changes to take effect.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        if (settings.getLogType() == 0) {
            settings.setCurrentLogFile(settings.getFlashLogFileName(), false);
        } else {
            settings.setCurrentLogFile(settings.getPolicyLogFileName(), false);
        }
    }

    private void setHighlightAll(boolean selected, boolean uiTrigger) {
        settings.setHighlightAll(selected, uiTrigger);
    }

    private void setFilter(boolean selected, boolean uiTrigger) {
        settings.setFilter(selected, uiTrigger);
    }

    public void clearTraceClicked() {
        new DeleteFile(settings.getCurrentLogFile());
        settings.clearTrace(true);
    }

    public void textAreaMousePressed() {
        if (settings.isAutoRefresh()) {
            stopReadLogFileTimer();
        }
        hideCodePopup();
        stopHideCodePopupTimer();
    }

    public void textAreaRightClicked(Point pt) {
        if (!settings.isEnableCodePopup()) {
            return;
        }

        hideCodePopup();

        int offset = view.getTextArea().viewToModel(pt);
        SourceAndLine source = null;
        try {
            source = settings.getHandleWordAtPosition().checkSourceFile(offset, false);
        } catch (Exception ex) {
            log.warn("onShowCodePopup 1()", ex);
        }
        if (source != null) {
            settings.getCodePopupHandler().show(pt, source);
            return;
        }

        String checkJSON = null;
        try {
            checkJSON = settings.getHandleWordAtPosition().checkJSON(offset);
        } catch (Exception ex) {
            log.warn("onShowCodePopup 2()", ex);
        }
        if (checkJSON != null) {
            settings.getCodePopupHandler().show(pt, checkJSON);
            return;
        }
    }

    public void textAreaDoubleClicked(Point pt) {
        int viewToModel = view.getTextArea().viewToModel(pt);
        SwingUtilities.invokeLater(new HW(viewToModel));
    }

    public void detectFlashPlayer() {
        DebugPlayerDetector d = new DebugPlayerDetector();
        d.detect();
    }

    private void initKeyBindings() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if ((e.isControlDown() || e.isMetaDown())
                            && e.getKeyCode() == KeyEvent.VK_F) {
                        searchPanelVisibleClicked();
                        return true;
                    } else if ((e.isControlDown() || e.isMetaDown())
                            && e.getKeyCode() == KeyEvent.VK_W) {
                        wordWrapClicked();
                        return true;
                    }
                }
                return false;
            }
        });

    }

    class HW implements Runnable {

        private final int offset;

        public HW(int offset) {
            this.offset = offset;
        }

        public void run() {
//            int selectionStart = view.getTextArea().getSelectionStart();
//            int selectionEnd = view.getTextArea().getSelectionEnd();
//            view.getTextArea().setSelectionStart(offset);
//            view.getTextArea().setSelectionEnd(offset);
            if (!handleWordAtPosition(offset)) {
//                if (selectionEnd > -1 && selectionStart > -1) {
//                    view.getTextArea().setSelectionStart(selectionStart);
//                    view.getTextArea().setSelectionEnd(selectionEnd);
//                }
            }
        }
    }

    public void textAreaMouseReleased() {
        if (settings.isAutoRefresh()) {
            startReadLogFileTimer();
        }
    }

    public void clearSearchClicked() {
        settings.getSearcher().clearSearch();
        highlightStackTraceErrors();
        settings.clearSearch();
    }

    public void setLogTypeClicked(String value) {
        setLogType(value, true);
        createReadLogTimerTask().run();
        if (settings.isAutoRefresh()) {
            startReadLogFileTimer();
        }
    }

    public void copyAllClicked(String text) {
        TextTransfer tt = new TextTransfer();
        tt.setClipboardContents(text);
    }

    public void openOptionsClicked() {
        settings.setAlwaysOnTopUI(false, true);
        if (optionsForm == null) {
            optionsForm = new OptionsForm(view.getBounds(), this, settings);
        }

        optionsForm.setVisible(true);
    }

    public void searchPanelVisibleClicked() {
        settings.setSearchVisible(!settings.isSearchVisible(), true);
    }

    public void lineNumbersVisibleClicked() {
        settings.setLineNumbersVisible(!settings.isLineNumbersVisible(), true);
    }

    public void snapshotClicked(String text) {
        Point location = view.getLocation();
        SnapshotForm snapshotForm = new SnapshotForm(this, settings);
        snapshotForm.setLocation(location.x - snapshotForm.getWidth() - settings.getSnapshotForms().size() * 20,
                location.y - settings.getSnapshotForms().size() * 20);
        snapshotForm.setSize(snapshotForm.getWidth(), view.getHeight());
        snapshotForm.init(text);

        settings.getSnapshotForms().add(snapshotForm);
    }

    public void wordWrapClicked() {
        settings.setWordWrap(!settings.isWordWrap(), true);
        for (SnapshotForm snapshotForm : settings.getSnapshotForms()) {
            snapshotForm.setWordWrap(settings.isWordWrap());
        }

        if (settings.getSearcher().isWasSearching()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    startSearch(settings.getSearcher().getLastSearchPos(), false);
                    highlightStackTraceErrors();
                }
            });
        }
    }

    public void aboutOpenClicked() {
        settings.setAlwaysOnTopUI(false, true);
        if (aboutForm == null) {
            aboutForm = new AboutPanel(view.getBounds(), this, settings);
        }

        aboutForm.setVisible(true);
    }

    public void textAreaMouseMoved(MouseEvent evt) {
        if (!settings.getCodePopupHandler().isVisible()) {
            // empty
        } else if (hideCodePopupTimer == null) {
            startHideCodePopupTimer();
        }
    }

    public void textAreaMouseExited(MouseEvent evt) {

    }

    public void formWindowDeactivated() {
        stopHideCodePopupTimer();
        hideCodePopup();
    }

    public void onClose() {
        settings.setMainWindowLocation(view.getBounds(), false);
        props.setProperty("settings.update.autoupdates", String.valueOf(settings.isCheckUpdates()));
        props.setProperty("settings.refresh_freq", String.valueOf(settings.getRefreshFreq()));
        props.setProperty("settings.flashlog.utf", String.valueOf(settings.isUTF()));
        props.setProperty("settings.flashlog.auto_refresh", String.valueOf(settings.isAutoRefresh()));
        props.setProperty("settings.always_on_top", String.valueOf(settings.isAlwaysOnTop()));
        props.setProperty("settings.highlight_all", String.valueOf(settings.isHightlightAll()));
        props.setProperty("settings.flashlog.word_wrap", String.valueOf(settings.isWordWrap()));
        props.setProperty("settings.font.name", settings.getTraceFont().getName());
        props.setProperty("settings.font.size", String.valueOf(settings.getTraceFont().getSize()));
        props.setProperty("settings.flashlog.filename", settings.getFlashLogFileName());
        props.setProperty("settings.window.x", String.valueOf(settings.getMainWindowLocation().getX()));
        props.setProperty("settings.window.y", String.valueOf(settings.getMainWindowLocation().getY()));
        props.setProperty("settings.window.width", String.valueOf(settings.getMainWindowLocation().getWidth()));
        props.setProperty("settings.window.height", String.valueOf(settings.getMainWindowLocation().getHeight()));
        props.setProperty("settings.restore_on_trace_update", String.valueOf(settings.isRestoreOnUpdate()));
        props.setProperty("settings.flashlog.max_num_lines", String.valueOf(settings.getMaxNumLines()));
        props.setProperty("settings.flashlog.max_num_lines_enabled", String.valueOf(settings.isMaxNumLinesEnabled()));
        props.setProperty("settings.log_type", String.valueOf(settings.getLogType()));
        props.setProperty("settings.flashlog.highlight_errors_enabled", String.valueOf(settings.isHighlightStackTraceErrors()));
        props.setProperty("settings.enable_code_popups", String.valueOf(settings.isEnableCodePopup()));
        props.setProperty("settings.search_panel_visible", String.valueOf(settings.isSearchVisible()));
        props.setProperty("settings.line_numbers_visible", String.valueOf(settings.isLineNumbersVisible()));
        props.setProperty("settings.enable_trace_click", String.valueOf(settings.isEnableTraceClick()));
        props.setProperty("settings.custom_as_editor", String.valueOf(settings.getCustomASEditor()));
        props.setProperty("settings.use_custom_as_editor", String.valueOf(settings.isDefaultASEditor()));
        props.setProperty("settings.new_features_shown" + Conf.VERSION, String.valueOf(settings.wasNewFeaturesPanelShown()));
        props.setProperty("settings.flashlog.vizzy_trace_enabled", String.valueOf(settings.isEnableParsingSourceLines()));
        props.setProperty("settings.update.last", String.valueOf(settings.getLastUpdateDate().getTime()));
        props.setProperty("settings.font.color", String.valueOf(settings.getFontColor().getRGB()));
        props.setProperty("settings.text_area.color", String.valueOf(settings.getBgColor().getRGB()));

        StringBuilder keywords = new StringBuilder();
        DefaultComboBoxModel searchKeywordsModel = settings.getSearchKeywordsModel();
        for (int i = 0; i < searchKeywordsModel.getSize(); i++) {
            String keyword = (String) searchKeywordsModel.getElementAt(i);
            if (i != 0) {
                keywords.append("|||");
            }
            if (!"".equals(keyword)) {
                keywords.append(keyword);
            }
        }
        props.setProperty("search.keywords", keywords.toString());

        try {
            props.store(new FileOutputStream(settings.getSettingsFile()), "");
        } catch (FileNotFoundException ex) {
            log.error("error saving setting 1. " + settings.getSettingsFile().getName());
            JOptionPane.showMessageDialog(null, "It was not possible to save settings file\n"
                    + "in '" + settings.getSettingsFile().getAbsolutePath() + "'.\n"
                    + "Probably this is a system folder (like c:\\Program Files or\n"
                    + "c:\\Windows). Please, move Vizzy outside current location\n"
                    + "and try again.", "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            log.error("error saving setting 2.");
        }
    }

//    public void searchKeyReleased(String text, KeyEvent evt) {
//        if (text == null) {
//            return;
//        }
//        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//            if (text.equals("")) {
//                clearSearchClicked();
//                return;
//            }
//            settings.setUIActionsAvailable(false);
//            addSearchKeyword(text);
//            settings.getSearcher().setWord(text);
//            startSearch(true, true);
//            highlightStackTraceErrors();
//            settings.setUIActionsAvailable(true);
//        }
//    }
    public void highlightAllClicked(boolean selected) {
        setHighlightAll(selected, true);
        if (settings.getSearcher().isWasSearching()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    startSearch(settings.getSearcher().getLastSearchPos(), true);
                    highlightStackTraceErrors();
                }
            });
        }
    }

    public void filterClicked(boolean selected, String text) {
        DefaultComboBoxModel searchKeywordsModel = settings.getSearchKeywordsModel();
        if (searchKeywordsModel.getIndexOf(text) == -1) {
            searchComboboxChanged(text, true);
        }

        setFilter(selected, true);

        if (settings.getSearcher().isWasSearching()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    startSearch(settings.getSearcher().getLastSearchPos(), true);
                    highlightStackTraceErrors();
                }
            });
        }
    }

    private void startSearch(int position, boolean scrollToSearchResult) {
        startSearch(settings.getSearcher().getWord(), position, scrollToSearchResult);
    }

    private void startSearch(String word, int position, boolean scrollToSearchResult) {
        if (word == null || word.equals("")) {
            return;
        }

        if (settings.isFilter()) {
            settings.beforeFilter();
            String content;
            try {
                content = settings.getSearcher().filter(word, settings.getTraceContent());
                settings.afterFilter(content);
            } catch (Exception ex) {
                log.warn("filtering exception", ex);
            }
        } else {
            SearchResult searchResult = settings.getSearcher().search(word, settings.getTraceContent(), position);
            settings.search(word, searchResult, scrollToSearchResult);
        }
    }

    public void textAreaKeyPressed(String text, KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_F3
                && text != null
                && text.length() > 0
                && !settings.isFilter()) {
            boolean isNewSearch = !text.equals(settings.getSearcher().getWord());
            settings.setUIActionsAvailable(false);
            settings.highlightTraceKeyword(text);
            addSearchKeyword(text);
            startSearch(text,
                    isNewSearch ? view.getTextArea().getCaretPosition() : settings.getSearcher().getNextSearchPos(),
                    true);
            highlightStackTraceErrors();
            settings.setUIActionsAvailable(true);
        }
    }

    public void autoRefreshClicked(boolean selected) {
        settings.setAutoRefresh(selected, true);
        if (settings.isAutoRefresh()) {
            createReadLogTimerTask().run();
            startReadLogFileTimer();
        } else {
            stopReadLogFileTimer();
        }
    }

    private void addSearchKeyword(String selectedItem) {
        DefaultComboBoxModel searchKeywordsModel = settings.getSearchKeywordsModel();
        if (searchKeywordsModel.getIndexOf(selectedItem) == -1) {
            searchKeywordsModel.insertElementAt(selectedItem, 0);
            if (searchKeywordsModel.getSize() > 7) {
                searchKeywordsModel.removeElementAt(7);
            }
        }
    }

    public void alwaysOnTopClicked(boolean selected) {
        settings.setAlwaysOnTop(selected, true);
    }

    public void aboutOKClick() {
        aboutForm.setVisible(false);
        settings.setAlwaysOnTop(settings.isAlwaysOnTop(), true);
    }

    public void snapshotFormsClose(SnapshotForm frame) {
        settings.getSnapshotForms().remove(frame);
    }

    public void optionsCancelled() {
        optionsForm.setVisible(false);
        settings.setAlwaysOnTopUI(settings.isAlwaysOnTop(), true);
    }

    public void optionsOK(SettingsModel s, HashMap<String, String> mmCFG) {
        settings.setUIActionsAvailable(false);
        settings.setTraceFont(s.getTraceFont(), true);
        settings.setFontColor(s.getFontColor(), true);
        settings.setBgColor(s.getBgColor(), true);

        settings.setFlashLogFileName(s.getFlashLogFileName(), true);
        if (settings.getLogType() == 0) {
            settings.setCurrentLogFile(s.getFlashLogFileName(), true);
        } else if (settings.getLogType() == 1) {
            settings.setCurrentLogFile(settings.getPolicyLogFileName(), true);
        }
        settings.setCheckUpdates(s.isCheckUpdates(), true);
        settings.setMaxNumLinesEnabled(s.isMaxNumLinesEnabled(), true);
        settings.setMaxNumLines(s.getMaxNumLines(), true);
        settings.setUTF(s.isUTF(), true);
        settings.setRefreshFreq(s.getRefreshFreq(), true);
        settings.setRestoreOnUpdate(s.isRestoreOnUpdate(), true);
        settings.setHighlightStackTraceErrors(s.isHighlightStackTraceErrors(), true);
        settings.setCustomASEditor(s.getCustomASEditor(), true);
        settings.setDefaultASEditor(s.isDefaultASEditor(), true);
        settings.setEnableCodePopup(s.isEnableCodePopup(), true);
        settings.setEnableTraceClick(s.isEnableTraceClick(), true);
        settings.setEnableParsingSourceLines(s.isEnableParsingSourceLines(), true);

        mmcfgInitializer.saveKeys(mmCFG);

        settings.setRecentHash(null, true);
        createReadLogTimerTask().run();
        if (settings.isAutoRefresh()) {
            startReadLogFileTimer();
        } else {
            stopReadLogFileTimer();
        }

        optionsForm.setVisible(false);
        settings.setAlwaysOnTopUI(settings.isAlwaysOnTop(), true);

        settings.setUIActionsAvailable(true);
        settings.optionsClosed();
    }

    public void traceAreaMouseWheel(MouseWheelEvent evt) {
        hideCodePopup();
    }

    public void newFeaturesPanelClosed() {
        settings.setNewFeaturesPanelShown(true, true);
    }

    public void searchComboboxChanged(String text, boolean enterKey) {
        if (text == null) {
            return;
        }
        if (text.equals("")) {
            clearSearchClicked();
            return;
        }
        if (text.length() < 3 && !enterKey && !settings.isFilter()) {
            return;
        }
        boolean isNewSearch = !text.equals(settings.getSearcher().getWord());
        int position;
        if (isNewSearch) {
            position = 0;
        } else {
            position = settings.getSearcher().getNextSearchPos();
        }
        settings.setUIActionsAvailable(false);
        if (enterKey) {
            addSearchKeyword(text);
        }
        startSearch(text, position, true);
        highlightStackTraceErrors();
        settings.setUIActionsAvailable(true);
    }

    public void regexpClicked(boolean selected) {
        settings.setRegexp(selected, true);
        if (settings.getSearcher().isWasSearching()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    startSearch(settings.getSearcher().getLastSearchPos(), true);
                    highlightStackTraceErrors();
                }
            });
        }
    }

    private void saveBeforeExit() {
        onClose();
        settings.closeApp();
        System.exit(0);
    }
}
