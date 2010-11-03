/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vizzy.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import vizzy.model.Conf;
import vizzy.util.DefaultHashMap;

/**
 *
 * @author sergeil
 */
public class MMCFGInitializer {

    private static final Logger log = Logger.getLogger(MMCFGInitializer.class);

    private File mmcfg;
    private DefaultHashMap<String, String> map;

    private boolean isMmcfgCreated;
    private boolean isPolicyFileRecorded;
    private String traceFileLocation;

    public MMCFGInitializer() {
        map = new DefaultHashMap<String, String>();
    }

    public void init() {

        try {
            String mmcfgPath = evaluateMMCFGPath();
            if (mmcfgPath == null) {
                return;
            }

            mmcfg = new File(mmcfgPath);
            
            if (mmcfg.exists()) {

                readMMCFG();

                if (map.containsKey("TraceOutputFileName")) {
                    setTraceFileLocation(map.get("TraceOutputFileName"));
                }
                if (map.containsKey("PolicyFileLog")) {
                    setPolicyFileRecorded(true);
                }

            } else {
                HashMap<String, String> m = new HashMap<String, String>();
                m.put("TraceOutputFileEnable", "1");
                m.put("ErrorReportingEnable", "1");
                m.put("PolicyFileLog", "1");
                m.put("PolicyFileLogAppend", "1");
                saveKeys(m);
                setMmcfgCreated(true);
            }
        } catch (Exception ex) {
            log.warn("init()", ex);
        }
    }

    public void recordPolicyFile() {
        try {
            HashMap<String, String> m = new HashMap<String, String>();
            m.put("PolicyFileLog", "1");
            m.put("PolicyFileLogAppend", "1");
            saveKeys(map);
            isPolicyFileRecorded = true;
        } catch (Exception e) {
            log.warn("recordPolicyFile() ", e);
        }
    }

    public void saveKeys(Map<String, String> m) {
        map.putAll(m);
        writeMMCFG();
    }

    private void writeMMCFG() {
        try {
            ArrayList<String> lines = new ArrayList<String>();
            Set<String> keys = map.keySet();
            for (String key : keys) {
                String v = map.get(key);
                if (v == null) {
                    lines.add(key);
                } else {
                    lines.add(key + "=" + v);
                }
            }
            FileUtils.writeLines(mmcfg, "UTF-8", lines, Conf.newLine);
        } catch (Exception ex) {
            log.warn("writeMMCFG", ex);
        }
    }
    
    private void readMMCFG() {
        try {
            List readLines = FileUtils.readLines(mmcfg, "UTF-8");
            map = new DefaultHashMap<String, String>();
            for (Object object : readLines) {
                String[] k = ((String)object).split("=");
                if (k[0].length() ==  0) {
                    
                } else if (k.length == 1) {
                    map.put(k[0], null);
                } else {
                    map.put(k[0], k[1]);
                }
            }
        } catch (IOException ex) {
            log.warn("readMMCFG", ex);
        }
    }

    private String evaluateMMCFGPath() {
        String osName = Conf.OSName;
        if (osName == null) {
            return null;
        }
        String homeDir = Conf.userHome;
        String mmcfgPath = null;
        if (osName.indexOf(Conf.OS_WINDOWS_VISTA) > -1) {
            if (homeDir == null) {
                return null;
            }
            mmcfgPath = homeDir + File.separator + "mm.cfg";
        } else if (osName.indexOf(Conf.OS_WINDOWS) > -1) {
            if (homeDir == null) {
                return null;
            }
            mmcfgPath = homeDir + File.separator + "mm.cfg";
        } else if (osName.indexOf(Conf.OS_MAC_OS_X) > -1) {
            mmcfgPath = "/Library/Application Support/Macromedia/mm.cfg";
        } else if (osName.indexOf(Conf.OS_LINUX) > -1) {
            if (homeDir == null) {
                return null;
            }
            mmcfgPath = homeDir + File.separator + "mm.cfg";
        }
        return mmcfgPath;
    }




    public String getTraceFileLocation() {
        return traceFileLocation;
    }
    public boolean isPolicyFileRecorded() {
        return isPolicyFileRecorded;
    }

    public void setPolicyFileRecorded(boolean isPolicyFileRecorded) {
        this.isPolicyFileRecorded = isPolicyFileRecorded;
    }

    private void setTraceFileLocation(String traceFileLocation) {
        this.traceFileLocation = traceFileLocation;
    }

    public boolean isMmcfgCreated() {
        return isMmcfgCreated;
    }

    public void setMmcfgCreated(boolean mmcfgCreated) {
        this.isMmcfgCreated = mmcfgCreated;
    }

    public DefaultHashMap<String, String> getMmcfgKeys() {
        return map;
    }

    public void setMmcfgKeys(DefaultHashMap<String, String> map) {
        this.map = map;
    }

}
