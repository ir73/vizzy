/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vizzy.model;

import java.awt.Color;
import vizzy.util.TextAreaHighlightPainter;

/**
 *
 * @author sergei
 */
public class Conf {

    public static final String OS_LINUX = "linux";
    public static final String OS_MAC_OS_X = "mac os x";
    public static final String OS_WINDOWS = "windows";
    public static final String OS_WINDOWS_VISTA = "vista";

    public static final String OSName = System.getProperty("os.name").toLowerCase();
    public static final String newLine = System.getProperty("line.separator");

    public static String userHome;
    static {
        if (Conf.OSName.indexOf(Conf.OS_WINDOWS) > -1) {
            userHome = System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH");
        } else {
            userHome = System.getProperty("user.home");
        }
    }
    
    public static String OSShortName;

    static {
        if (OS_LINUX.equals(OSName)) {
            Conf.OSShortName = "linux";
        } else if (OS_MAC_OS_X.equals(OSName)) {
            Conf.OSShortName = "mac";
        } else {
            Conf.OSShortName = "win";
        }
    }

    public static final String POLICY_LOG_FILE_NAME = "policyfiles.txt";

    public static final TextAreaHighlightPainter mouseOverObjectPainter = new TextAreaHighlightPainter(new Color(190, 230, 100));
    public static final TextAreaHighlightPainter highlightedSearchResultPainter = new TextAreaHighlightPainter(new Color(0, 150, 0));
    public static final TextAreaHighlightPainter searchResultPainter = new TextAreaHighlightPainter(new Color(150, 235, 150));
    public static final TextAreaHighlightPainter errorPainter = new TextAreaHighlightPainter(new Color(200, 200, 200));
    public static final TextAreaHighlightPainter warningPainter = new TextAreaHighlightPainter(new Color(230, 230, 230));
    public static final Color DEFAULT_SEARCH_COMBO_COLOR = Color.white;
    public static final Color FOUND_SEARCH_COMBO_COLOR = new Color(150, 255, 150);
    public static final Color NOTFOUND_SEARCH_COMBO_COLOR = new Color(255, 150, 150);

    public static final String VERSION = "3.7";
    public static final String VIZZY_PROPERTIES_FILENAME = "vizzy-settings.pro";
    public static String vizzyRootDir;
    public static final String URL_PROJECT_HOME = "http://code.google.com/p/flash-tracer/";
    public static final String URL_PROJECT_DOWNLOAD = "http://code.google.com/p/flash-tracer/downloads/list";
    public static final String URL_DONATION = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=JPVRL8G9JYAVL&lc=EE&item_name=Vizzy%20Flash%20Tracer&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted";
    public static final String URL_MAILTO = "mailto:sergei.ledvanov@gmail.com";
    public static final String URL_VIZZY_WIKI_TRACE = "http://code.google.com/p/flash-tracer/wiki/VizzyTrace";
    public static final String URL_VIZZY_WIKI_CLICKABLE = "http://code.google.com/p/flash-tracer/wiki/Features#HTTP_Links_are_Clickable";
    public static final String URL_VIZZY_WIKI_CODE_POPUP = "http://code.google.com/p/flash-tracer/wiki/Features#Explore_Source_Code";
    public static final String URL_VIZZY_PLUGIN = "http://code.google.com/p/flash-tracer/wiki/FlashDevelopPlugin";
    public static final String FLASHDEVELOP_PATH = "C:\\Program Files\\FlashDevelop\\FlashDevelop.exe";
    public static final String DEFAULT_WINDOWS_EDITOR_PATH = "\"" + FLASHDEVELOP_PATH + "\" \"%file%\" -line %line%";
    public static final String DEFAULT_MAC_EDITOR_PATH = "/Applications/TextEdit.app/Contents/MacOS/TextEdit %file%";
    public static final String DEFAULT_LINUX_EDITOR_PATH = "gedit %file%";
    public static final String UPDATE_FOLDER = "/.update";
    public static final long UPDATE_CHECK_FREQ = 7L * 24L * 60L * 60L * 1000L;

    public static final String WEBSITE_UPDATE_PHRASE = "Current version is: ";
    public static final String WEBSITE_FEATURES_PHRASE = "Current version features: ";
    public static final long MAX_NUM_LINES_OUTOFMEMORY = 50000;
    public static final long MAX_TIMER_BEFORE_OUTOFMEMORY = 3000;

}
