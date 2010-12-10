/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.listeners;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import vizzy.model.SearchResult;

/**
 *
 * @author sergeil
 */
public interface ISettingsListener {

    public void onCustomASEditorChanged(String customASEditor);

    public void onRefreshFreqChanged(long refreshFreq);

    public void onLogTypeChanged(int logType);

    public void onHightlightAllChanged(boolean hightlightAll);

    public void onDefaultASEditorChanged(boolean defaultASEditor);

    public void onUTFChanged(boolean uTF);

    public void onHighlightStackTraceErrorsChanged(boolean highlightKeywords);

    public void onPolicyLogFileNameChanged(String policyLogFileName);

    public void onFlashLogFileNameChanged(String flashLogFileName);

    public void onFontsChanged(Font[] fonts);

    public void onMainWindowLocationChanged(Rectangle window);

    public void onCurrentLogFileChanged(String currentLogFile);

    public void onTraceFontChanged(Font traceFont);

    public void onSearchKeywordsChanged(String[] searchKeywords, DefaultComboBoxModel searchKeywordsModel);

    public void onFilterChanged(boolean filter);

    public void onAutoRefreshChanged(boolean autoRefresh);

    public void onWordWrapChanged(boolean wordWrap);

    public void onCheckUpdatesChanged(boolean checkUpdates);

    public void onMaxNumLinesEnabledChanged(boolean maxNumLinesEnabled);

    public void onMaxNumLinesChanged(long maxNumLines);

    public void onRestoreOnUpdateChanged(boolean restoreOnUpdate);

    public void onLastUpdateDateChanged(Date lastUpdateDate);

    public void onDefaultFontChanged(String defaultFont);

    public void onSetFontNamesChanged(String[] fontNames);

    public void onRecentHashChanged(String recentHash);

    public void onTraceContentChanged(String traceContent);

    public void onSearch(String word, SearchResult searchResult, boolean scrollToSearchResult);

    public void onOptionsClosed();

    public void onHighlightTraceKeyword(String text);

    public void onSearchCleared();

    public void onAlwaysOnTopChanged(boolean alwaysOnTop);

    public void onAlwaysOnTopUIChanged(boolean alwaysOnTop);

    public void onInit();

    public void onAfterInit();

    public void onEnableTraceClickChanged(boolean enableStackTraceClick);

    public void onEnableCodePopupChanged(boolean enableCodePopup);

    public void onShowNewFeaturesPanel();

    public void onNewFeaturesPanelShownChanged(boolean wasNewFeaturesPanelShown);

    public void onRegexpChanged(boolean regexp);

    public void beforeFilter();

    public void afterFilter(String content);

    public void onFontColorChanged(Color fontColor);

    public void onBgColorChanged(Color bgColor);

    public void onEnableParsingSourceLines(boolean enableParsingSourceLines);

    public void closeApp();

    public void onProgramFilesDetected(boolean programFilesDetected);

    public void onSearchVisible(boolean searchVisible);

}
