/*
 * DeleteFile.java
 *
 * Created on 21 March 2007, 20:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package vizzy.tasks;

import java.io.File;
import org.apache.log4j.Logger;

/**
 *
 * @author Admin
 */
public class DeleteFile {

    private static final Logger log = Logger.getLogger(DeleteFile.class);
    
    public DeleteFile(String file) {
        try {
            
            File bkup = new File(file);
            
            if (bkup.exists()) {
                bkup.delete();
            }
        } catch (Exception ex) {
            log.warn("error deleting log.");
        }
    }
    
}
